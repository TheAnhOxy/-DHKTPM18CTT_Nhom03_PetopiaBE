package com.pet.service.impl;

import com.pet.converter.OrderConverter;
import com.pet.entity.*;
import com.pet.enums.*;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.OrderCreateRequestDTO;
import com.pet.modal.request.OrderItemRequestDTO;
import com.pet.modal.response.OrderResponseDTO;
import com.pet.modal.response.PageResponse;
import com.pet.repository.*;
import com.pet.service.EmailService;
import com.pet.service.OrderService;
import com.pet.service.SePayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private SePayService sePayService;
    @Autowired private EmailService emailService;
    @Autowired private OrderConverter orderConverter;
    @Autowired private OrderItemRepository orderItemRepository;

    //  Tạo Đơn Hàng ---
    @Override
    @Transactional
    public OrderResponseDTO createOrder(String userId, OrderCreateRequestDTO request) {
        //  Lấy User (Đã đăng nhập)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        // --- CẬP NHẬT TÊN NGƯỜI NHẬN VÀO USER ---
        // Nếu form có gửi tên người nhận, ta cập nhật vào hồ sơ User luôn
        if (request.getRecipientName() != null && !request.getRecipientName().trim().isEmpty()) {
            user.setFullName(request.getRecipientName());
            // Lưu user lại để cập nhật fullName mới nhất
            userRepository.save(user);
        }
        //  XỬ LÝ ĐỊA CHỈ (Logic mới)
        Address shippingAddress;
        // Nếu User nhập địa chỉ mới (không truyền ID hoặc ID rỗng)
        if (request.getAddressId() == null || request.getAddressId().isEmpty()) {
            // Validate form
            if (request.getNewProvince() == null || request.getNewStreet() == null) {
                throw new IllegalArgumentException("Vui lòng nhập đầy đủ địa chỉ");
            }
            // Tạo và lưu địa chỉ mới cho User
            shippingAddress = createNewAddressForUser(user, request);
        } else {
            // Dùng địa chỉ cũ
            shippingAddress = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        }

        // 3. Tạo Order
        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setUser(user);
        order.setAddress(shippingAddress);
        order.setPhoneNumber(request.getPhoneNumber()); // Lấy sđt từ form nhập
        order.setNote(request.getNote());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(OrderPaymentStatus.UNPAID);
        order.setShippingFee(30000.0); // Phí ship mặc định

        //  Xử lý Items & Tính tiền (Giữ nguyên logic cũ)
        double itemsTotal = 0;
        Set<OrderItem> orderItems = new HashSet<>();
        long currentTime = System.currentTimeMillis();
        int i = 0;

        for (OrderItemRequestDTO itemReq : request.getItems()) {
            Pet pet = petRepository.findById(itemReq.getPetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet not found"));

            if (pet.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + pet.getName() + " hết hàng");
            }
            pet.setStockQuantity(pet.getStockQuantity() - itemReq.getQuantity());
            petRepository.save(pet);

            OrderItem oi = new OrderItem();
            oi.setOrderItemId("OI" + currentTime + (i++));
            oi.setOrder(order);
            oi.setPet(pet);
            oi.setQuantity(itemReq.getQuantity());
            oi.setPriceAtPurchase(pet.getPrice());

            itemsTotal += pet.getPrice() * itemReq.getQuantity();
            orderItems.add(oi);
        }
        order.setOrderItems(orderItems);
        order.setTotalAmount(itemsTotal + order.getShippingFee());

        Order savedOrder = orderRepository.save(order);

        //  XỬ LÝ THANH TOÁN (Payment Logic)
        handlePaymentAndEmail(savedOrder, request.getPaymentMethod());

        return orderConverter.toResponseDTO(savedOrder);
    }

    // --- Helper: Tạo địa chỉ mới ---
    private Address createNewAddressForUser(User user, OrderCreateRequestDTO req) {
        Address address = new Address();
        address.setAddressId(generateAddressId()); // ADxxx
        address.setUser(user);
        address.setProvince(req.getNewProvince());
        address.setDistrict(req.getNewDistrict());
        address.setWard(req.getNewWard());
        address.setStreet(req.getNewStreet());
        address.setCountry("Vietnam");

        // Nếu user chưa có địa chỉ nào -> Set cái này là default
        boolean hasAddress = addressRepository.countByUser_UserId(user.getUserId()) > 0;
        address.setIsDefault(!hasAddress);

        return addressRepository.save(address);
    }

    // --- Helper: Xử lý Payment & Gửi Mail ---
    private void handlePaymentAndEmail(Order order, PaymentMethod method) {
        Payment payment = new Payment();
        payment.setPaymentId(generatePaymentId());
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());

        String emailSubject;
        String emailContent;

        // CASE 1: CHUYỂN KHOẢN NGÂN HÀNG (Hiện QR)
        if (method == PaymentMethod.BANK_TRANSFER) {
            String content = "THANHTOAN " + order.getOrderId();
            String qrUrl = sePayService.generateQrUrl(order.getTotalAmount(), content);

            payment.setPaymentUrl(qrUrl);
            payment.setTransactionId(content);

            emailSubject = "[Petopia] Vui lòng thanh toán đơn hàng #" + order.getOrderId();
            emailContent = buildBankTransferEmail(order, qrUrl, content);
        }
        // CASE 2: TIỀN MẶT (COD) - Không QR
        else {
            payment.setPaymentUrl(null);
            payment.setTransactionId(null);

            emailSubject = "[Petopia] Đặt hàng thành công #" + order.getOrderId();
            emailContent = buildCodEmail(order);
        }

        paymentRepository.save(payment);

        // Gửi mail
        if (order.getUser().getEmail() != null) {
            emailService.sendEmail(order.getUser().getEmail(), emailSubject, emailContent);
        }
    }

    private String buildBankTransferEmail(Order order, String qrUrl, String content) {
        return String.format("""
            <div style="font-family: Arial, sans-serif;">
                <h2 style="color: #2c3e50;">Cảm ơn bạn đã đặt hàng!</h2>
                <p>Đơn hàng <strong>%s</strong> đang chờ thanh toán.</p>
                <p>Tổng tiền: <strong style="font-size: 18px; color: #e74c3c;">%,.0f VNĐ</strong></p>
                
                <div style="border: 2px dashed #3498db; padding: 15px; text-align: center; margin: 20px 0;">
                    <p>Quét mã QR để thanh toán ngay:</p>
                    <img src="%s" alt="QR SePay" width="250" />
                    <p style="margin-top: 10px;">Hoặc chuyển khoản với nội dung: <strong style="background: #f1c40f; padding: 5px;">%s</strong></p>
                </div>
                <p>Đơn hàng sẽ được xử lý ngay sau khi chúng tôi nhận được tiền.</p>
            </div>
            """, order.getOrderId(), order.getTotalAmount(), qrUrl, content);
    }

    private String buildCodEmail(Order order) {
        return String.format("""
            <div style="font-family: Arial, sans-serif;">
                <h2 style="color: #27ae60;">Đặt hàng thành công!</h2>
                <p>Xin chào <strong>%s</strong>,</p>
                <p>Đơn hàng <strong>%s</strong> của bạn đã được ghi nhận.</p>
                <p>Tổng tiền: <strong>%,.0f VNĐ</strong></p>
                <p>Hình thức: <strong>Thanh toán khi nhận hàng (COD)</strong></p>
                <p>Chúng tôi sẽ sớm liên hệ để giao hàng đến địa chỉ: %s</p>
                <br/>
                <p>Cảm ơn bạn đã tin tưởng Petopia!</p>
            </div>
            """, order.getUser().getFullName(), order.getOrderId(), order.getTotalAmount(),
                order.getAddress().getStreet() + ", " + order.getAddress().getProvince());
    }

    // Helper: Tạo Payment
    private void createPaymentRecord(Order order, PaymentMethod method) {
        Payment payment = new Payment();
        payment.setPaymentId(generatePaymentId()); // PMxxx
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());

        // Nếu là Chuyển khoản -> Tạo QR SePay
        if (method == PaymentMethod.BANK_TRANSFER) {
            // Nội dung CK: "THANHTOAN [Mã Đơn]"
            String content = "THANHTOAN " + order.getOrderId();
            String qrUrl = sePayService.generateQrUrl(order.getTotalAmount(), content);

            payment.setPaymentUrl(qrUrl); // Lưu link QR vào DB
            payment.setTransactionId(content); // Lưu nội dung ck để đối soát
        }

        paymentRepository.save(payment);
    }

    // Helper: Gửi Email
    private void sendOrderConfirmationEmail(Order order, PaymentMethod method) {
        if (order.getUser().getEmail() == null) return;

        String qrSection = "";
        if (method == PaymentMethod.BANK_TRANSFER) {
            // Lấy QR Url từ payment vừa tạo
            Payment p = paymentRepository.findFirstByOrder_OrderIdOrderByCreatedAtDesc(order.getOrderId()).orElse(null);
            if (p != null) {
                qrSection = String.format("""
                    <div style="text-align: center; margin: 20px 0;">
                        <p>Vui lòng quét mã QR bên dưới để thanh toán:</p>
                        <img src="%s" alt="QR Code" width="200" style="border: 1px solid #ccc;"/>
                        <p>Nội dung CK: <strong>%s</strong></p>
                    </div>
                    """, p.getPaymentUrl(), p.getTransactionId());
            }
        }

        String htmlContent = String.format("""
            <h3>Cảm ơn bạn đã đặt hàng tại Petopia!</h3>
            <p>Mã đơn hàng: <strong>%s</strong></p>
            <p>Tổng tiền: <strong>%,.0f VNĐ</strong></p>
            %s
            <p>Chúng tôi sẽ sớm liên hệ để giao hàng.</p>
            """, order.getOrderId(), order.getTotalAmount(), qrSection);

        emailService.sendEmail(order.getUser().getEmail(), "Xác nhận đơn hàng #" + order.getOrderId(), htmlContent);
    }

    private String generatePaymentId() {
        String lastId = paymentRepository.findLastPaymentId().orElse("PM000");
        try { return String.format("PM%03d", Integer.parseInt(lastId.substring(2)) + 1); }
        catch (Exception e) { return "PM" + System.currentTimeMillis(); }
    }

    // Admin Update Trạng Thái ---
    @Override
    @Transactional
    @CacheEvict(value = {"dashboard_general_stats", "dashboard_top_selling"}, allEntries = true)
    public OrderResponseDTO updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(status);
        // Nếu delivered -> update payment status thành PAID (nếu là COD)
        if (status == OrderStatus.DELIVERED) {
             order.setPaymentStatus(OrderPaymentStatus.PAID);
        }

        return orderConverter.toResponseDTO(orderRepository.save(order));
    }

    @Override
    public PageResponse<OrderResponseDTO> getMyOrders(String userId, int page, int size) {
        Page<Order> orders = orderRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return orderConverter.toPageResponse(orders);
    }

    @Override
    public PageResponse<OrderResponseDTO> getAllOrders(OrderStatus status, String keyword, int page, int size) {
        Page<Order> orders = orderRepository.searchOrders(status, keyword, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return orderConverter.toPageResponse(orders);
    }

    @Override
    public OrderResponseDTO getOrderDetail(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return orderConverter.toResponseDTO(order);
    }

    @Override
    public OrderResponseDTO getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return orderConverter.toResponseDTO(order);
    }

    private String generateOrderId() {
        String lastId = orderRepository.findLastOrderId().orElse("O000");
        try {
            int num = Integer.parseInt(lastId.substring(1));
            return String.format("O%03d", num + 1);
        } catch (Exception e) {
            return "O001";
        }
    }
    private String generateOrderItemId() {
        String lastId = orderItemRepository.findLastOrderItemId().orElse("OI000");
        try {
            int num = Integer.parseInt(lastId.substring(2));
            return String.format("OI%03d", num + 1);
        } catch (Exception e) {
            return "OI001";
        }
    }

    private String generateAddressId() {
        String lastId = addressRepository.findLastAddressId().orElse(null);
        if (lastId == null) return "A001";
        try {
            int num = Integer.parseInt(lastId.substring(2));
            return String.format("A%03d", num + 1);
        } catch (Exception e) {
            return "AD" + System.currentTimeMillis();
        }
    }


}