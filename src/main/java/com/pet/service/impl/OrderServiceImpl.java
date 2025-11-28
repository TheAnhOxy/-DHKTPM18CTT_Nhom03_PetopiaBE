package com.pet.service.impl;

import com.pet.converter.OrderConverter;
import com.pet.entity.*;
import com.pet.enums.*;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.OrderCreateRequestDTO;
import com.pet.modal.request.OrderItemRequestDTO;
import com.pet.modal.request.SePayWebhookDTO;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @Autowired private VoucherRepository voucherRepository;
    @Autowired private PromotionRepository promotionRepository;
    @Autowired private OrderPromotionRepository orderPromotionRepository;
    @Autowired private OrderVoucherRepository orderVoucherRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private DeliveryHistoryRepository deliveryHistoryRepository;

    //  Tạo Đơn Hàng ---
//    @Override
//    @Transactional
//    public OrderResponseDTO createOrder(String userId, OrderCreateRequestDTO request) {
//        //  Lấy User (Đã đăng nhập)
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//
//        // --- CẬP NHẬT TÊN NGƯỜI NHẬN VÀO USER ---
//        // Nếu form có gửi tên người nhận, ta cập nhật vào hồ sơ User luôn
//        if (request.getRecipientName() != null && !request.getRecipientName().trim().isEmpty()) {
//            user.setFullName(request.getRecipientName());
//            // Lưu user lại để cập nhật fullName mới nhất
//            userRepository.save(user);
//        }
//        //  XỬ LÝ ĐỊA CHỈ (Logic mới)
//        Address shippingAddress;
//        // Nếu User nhập địa chỉ mới (không truyền ID hoặc ID rỗng)
//        if (request.getAddressId() == null || request.getAddressId().isEmpty()) {
//            // Validate form
//            if (request.getNewProvince() == null || request.getNewStreet() == null) {
//                throw new IllegalArgumentException("Vui lòng nhập đầy đủ địa chỉ");
//            }
//            // Tạo và lưu địa chỉ mới cho User
//            shippingAddress = createNewAddressForUser(user, request);
//        } else {
//            // Dùng địa chỉ cũ
//            shippingAddress = addressRepository.findById(request.getAddressId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
//        }
//
//        //  Tạo Order
//        Order order = new Order();
//        order.setOrderId(generateOrderId());
//        order.setUser(user);
//        order.setAddress(shippingAddress);
//        order.setPhoneNumber(request.getPhoneNumber());
//        order.setNote(request.getNote());
//
//        // --- LOGIC TRẠNG THÁI THEO PAYMENT METHOD ---
//        if (request.getPaymentMethod() == PaymentMethod.COD) {
//            // COD: Mua luôn -> Hoàn thành luôn
//            order.setStatus(OrderStatus.DELIVERED);
//            order.setPaymentStatus(OrderPaymentStatus.PAID);
//        } else {
//            // BANK: Chờ chuyển khoản -> Confirmed nhưng chưa trả tiền
//            order.setStatus(OrderStatus.CONFIRMED);
//            order.setPaymentStatus(OrderPaymentStatus.UNPAID);
//        }
//        // -------------------------------------------
//
////        order.setShippingFee(30000.0);
//        order.setShippingFee(0.0);
//
//        //  Xử lý Items & Tính tiền (Giữ nguyên logic cũ)
//        double itemsTotal = 0;
//        Set<OrderItem> orderItems = new HashSet<>();
//        long currentTime = System.currentTimeMillis();
//        int i = 0;
//
//
//
//        for (OrderItemRequestDTO itemReq : request.getItems()) {
//            Pet pet = petRepository.findById(itemReq.getPetId())
//                    .orElseThrow(() -> new ResourceNotFoundException("Pet not found"));
//
//            if (pet.getStockQuantity() < itemReq.getQuantity()) {
//                throw new RuntimeException("Sản phẩm " + pet.getName() + " hết hàng");
//            }
//            pet.setStockQuantity(pet.getStockQuantity() - itemReq.getQuantity());
//            petRepository.save(pet);
//
//            OrderItem oi = new OrderItem();
//            oi.setOrderItemId(generateOrderItemId());
//            oi.setOrder(order);
//            oi.setPet(pet);
//            oi.setQuantity(itemReq.getQuantity());
//
//            // Ưu tiên giá khuyến mãi nếu có, không thì lấy giá gốc
//            Double finalPrice = (pet.getDiscountPrice() != null && pet.getDiscountPrice() > 0)
//                ? pet.getDiscountPrice()
//                : pet.getPrice();
//            oi.setPriceAtPurchase(finalPrice);
//
//            itemsTotal += finalPrice * itemReq.getQuantity();
//            orderItems.add(oi);
//        }
//        order.setOrderItems(orderItems);
//        order.setTotalAmount(itemsTotal + order.getShippingFee());
//
//        Order savedOrder = orderRepository.save(order);
//
//        //  XỬ LÝ THANH TOÁN (Payment Logic)
//        handlePaymentAndEmail(savedOrder, request.getPaymentMethod());
//
//        return orderConverter.toResponseDTO(savedOrder);
//    }
    @Override
    @Transactional
    public OrderResponseDTO createOrder(String userId, OrderCreateRequestDTO request) {
        //  Lấy User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getRecipientName() != null && !request.getRecipientName().trim().isEmpty()) {
            user.setFullName(request.getRecipientName());
            userRepository.save(user);
        }

        //  Xử lý địa chỉ
        Address shippingAddress;
        if (request.getAddressId() == null || request.getAddressId().isEmpty()) {
            if (request.getNewProvince() == null || request.getNewStreet() == null) {
                throw new IllegalArgumentException("Vui lòng nhập đầy đủ địa chỉ");
            }
            shippingAddress = createNewAddressForUser(user, request);
        } else {
            shippingAddress = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        }

        //  Khởi tạo Order
        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setUser(user);
        order.setAddress(shippingAddress);
        order.setPhoneNumber(request.getPhoneNumber());
        order.setNote(request.getNote());
        order.setCreatedAt(LocalDateTime.now());

        // Set trạng thái ban đầu
        if (request.getPaymentMethod() == PaymentMethod.COD) {
            order.setStatus(OrderStatus.DELIVERED); // Logic : COD coi như xong luôn? (Thường là Pending -> Shipping)
            order.setPaymentStatus(OrderPaymentStatus.PAID);
        } else {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(OrderPaymentStatus.UNPAID);
        }

        //  Xử lý Order Items & Tính Tạm tính (Subtotal)
        double itemsTotal = 0;
        Set<OrderItem> orderItems = new HashSet<>();

        for (OrderItemRequestDTO itemReq : request.getItems()) {
            Pet pet = petRepository.findById(itemReq.getPetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet not found: " + itemReq.getPetId()));

            // Check tồn kho
            if (pet.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + pet.getName() + " không đủ số lượng");
            }
            // Trừ kho
            pet.setStockQuantity(pet.getStockQuantity() - itemReq.getQuantity());
            petRepository.save(pet);

            // Lấy giá (ưu tiên giá giảm của sản phẩm)
            Double itemPrice = (pet.getDiscountPrice() != null && pet.getDiscountPrice() > 0)
                    ? pet.getDiscountPrice()
                    : pet.getPrice();

            itemsTotal += itemPrice * itemReq.getQuantity();

            // Tạo OrderItem
            OrderItem oi = new OrderItem();
            oi.setOrderItemId(generateOrderItemId());
            oi.setOrder(order);
            oi.setPet(pet);
            oi.setQuantity(itemReq.getQuantity());
            oi.setPriceAtPurchase(itemPrice);
            oi.setDiscountApplied(0.0); // Mặc định 0, sẽ tính sau nếu có promo theo sp
            orderItems.add(oi);
        }
        order.setOrderItems(orderItems);

        //  Xử lý Voucher & Promotion (Tính tổng giảm giá)
        double totalDiscount = 0;
        Set<OrderVoucher> orderVouchers = new HashSet<>();
        Set<OrderPromotion> orderPromotions = new HashSet<>();

        // Voucher (Người dùng chọn)
        if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
            for (String vId : request.getVoucherIds()) {
                Voucher voucher = voucherRepository.findById(vId).orElse(null);

                // Validate Voucher
                if (voucher != null && isValidVoucher(voucher, itemsTotal)) {
                    double discountVal = calculateDiscount(voucher.getDiscountType(), voucher.getDiscountValue(), itemsTotal);
                    totalDiscount += discountVal;

                    // Tạo OrderVoucher
                    OrderVoucher ov = new OrderVoucher();
                    ov.setOrderVoucherId(generateOrderVoucherId());
                    ov.setOrder(order);
                    ov.setVoucher(voucher);
                    ov.setDiscountApplied(discountVal);
                    orderVouchers.add(ov);

                    // Tăng số lượt dùng
                    voucher.setUsedCount(voucher.getUsedCount() + 1);
                    voucherRepository.save(voucher);
                }
            }
            order.setOrderVouchers(orderVouchers);
        }

        //  Promotion (Tự động áp dụng)
        List<Promotion> activePromos = promotionRepository.findActivePromotions(LocalDate.now());
        for (Promotion promo : activePromos) {
            // Logic check: Ví dụ promo cho đơn hàng > X tiền
            if (promo.getMinOrderAmount() != null && itemsTotal >= promo.getMinOrderAmount()) {
                double promoDiscount = 0;

                if (promo.getPromotionType() == PromotionType.DISCOUNT) {
                    // Giả sử Promotion cũng có discountValue (tiền mặt) hoặc %
                    // Ở đây demo giảm thẳng tiền
                    promoDiscount = promo.getDiscountValue();
                }
                // Logic khác: FREESHIP, BUNDLE... (tùy bạn implement thêm)

                totalDiscount += promoDiscount;

                OrderPromotion op = new OrderPromotion();
                op.setOrderPromotionId(generateOrderPromotionId());
                op.setOrder(order);
                op.setPromotion(promo);
                op.setDiscountApplied(promoDiscount);
                orderPromotions.add(op);

                // Tăng lượt dùng promo
                promo.setUsedCount(promo.getUsedCount() + 1);
                promotionRepository.save(promo);
            }
        }
        order.setOrderPromotions(orderPromotions);


        //  Tính Tổng Tiền Cuối Cùng
        double shippingFee = 0.0;
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(totalDiscount); // Tổng tiền được giảm

        // Công thức: Hàng + Ship - Giảm giá (Không âm)
        double finalAmount = (itemsTotal + shippingFee) - totalDiscount;
        order.setTotalAmount(Math.max(0, finalAmount));

        //  Lưu & Thanh toán
        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // Tạo Delivery ban đầu cho đơn
        createInitialDeliveryForOrder(savedOrder);

        handlePaymentAndEmail(savedOrder, request.getPaymentMethod());

        return orderConverter.toResponseDTO(savedOrder);
    }

    // --- CÁC HÀM HELPER ---

    private boolean isValidVoucher(Voucher v, double orderTotal) {
        if (v.getStatus() != PromotionVoucherStatus.ACTIVE) return false;
        if (v.getStartDate().isAfter(LocalDate.now()) || v.getEndDate().isBefore(LocalDate.now())) return false;
        if (v.getMinOrderAmount() != null && orderTotal < v.getMinOrderAmount()) return false;
        if (v.getMaxUsage() != null && v.getUsedCount() >= v.getMaxUsage()) return false;
        return true;
    }

    private double calculateDiscount(VoucherDiscountType type, Double value, double orderTotal) {
        if (type == VoucherDiscountType.PERCENTAGE) {
            // Ví dụ: Giảm 10% của 1.000.000 = 100.000
            return orderTotal * (value / 100.0);
        } else {
            // Giảm tiền mặt: 50.000
            return value;
        }
    }

    private String generateOrderPromotionId() {
        String lastId = orderPromotionRepository.findLastId().orElse("OP000");
        try {
            int num = Integer.parseInt(lastId.substring(2));
            return String.format("OP%03d", num + 1);
        } catch (Exception e) {
            // Fallback nếu lỗi
            return "OP" + System.currentTimeMillis();
        }
    }

    private String generateOrderVoucherId() {
        String lastId = orderVoucherRepository.findLastId().orElse("OV000");
        try {
            int num = Integer.parseInt(lastId.substring(2));
            return String.format("OV%03d", num + 1);
        } catch (Exception e) {
            // Fallback nếu lỗi
            return "OV" + System.currentTimeMillis();
        }
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

    private void handlePaymentAndEmail(Order order, PaymentMethod method) {
        Payment payment = new Payment();
        payment.setPaymentId(generatePaymentId());
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setPaymentDate(LocalDateTime.now());

        if (method == PaymentMethod.BANK_TRANSFER) {
            // BANK: Trạng thái PENDING, Tạo QR
            payment.setPaymentStatus(PaymentStatus.PENDING);
            String content = "SEVQR " + order.getOrderId();
            String qrUrl = sePayService.generateQrUrl(order.getTotalAmount(), content);
            payment.setPaymentUrl(qrUrl);
            payment.setTransactionId(content);
            paymentRepository.save(payment);

            // Gửi mail Yêu cầu thanh toán
            String emailSubject = "[Petopia] Vui lòng thanh toán đơn hàng #" + order.getOrderId();
            String emailContent = buildBankTransferEmail(order, qrUrl, content);
            if (order.getUser().getEmail() != null) {
                emailService.sendEmail(order.getUser().getEmail(), emailSubject, emailContent);
            }

        } else {
            // COD: Trạng thái SUCCESS luôn (vì coi như trả tiền mặt rồi)
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setPaymentUrl(null);
            payment.setTransactionId(null);
            paymentRepository.save(payment);

            // Gửi mail Thành công
            String emailSubject = "[Petopia] Đơn hàng hoàn tất #" + order.getOrderId();
            String emailContent = buildCodSuccessEmail(order);
            if (order.getUser().getEmail() != null) {
                emailService.sendEmail(order.getUser().getEmail(), emailSubject, emailContent);
            }
        }
    }

    @Transactional
    @Override
    public void processSePayPayment(SePayWebhookDTO webhookData) {
        // Lấy Mã đơn hàng từ nội dung chuyển khoản
        String orderId = extractOrderId(webhookData.resolveTransferContent());

        Payment payment = paymentRepository.findFirstByOrder_OrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch cho đơn: " + orderId));

        // Kiểm tra trạng thái hiện tại
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return; // Đã xử lý rồi thì bỏ qua
        }

        //  KIỂM TRA THỜI GIAN (Logic 10 phút)
        LocalDateTime createdTime = payment.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();

        // Nếu quá 10 phút -> Đánh dấu FAILED và không update đơn hàng
        if (createdTime.plusMinutes(10).isBefore(now)) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Giao dịch quá hạn 10 phút. Vui lòng liên hệ Admin.");
        }

        //  Kiểm tra số tiền (Phải chuyển đủ hoặc dư)
        if (webhookData.getTransferAmount() < payment.getAmount()) {
            // Có thể xử lý logic chuyển thiếu tiền ở đây
            throw new RuntimeException("Số tiền chuyển không đủ");
        }

        //UPDATE TRẠNG THÁI THÀNH CÔNG
        // Update Payment
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        // Update Order -> DELIVERED & PAID
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentStatus(OrderPaymentStatus.PAID);
        orderRepository.save(order);

        //  Gửi mail xác nhận thanh toán thành công
        emailService.sendEmail(
                order.getUser().getEmail(),
                "[Petopia] Thanh toán thành công đơn #" + orderId,
                buildPaymentSuccessEmail(order)
        );
    }

    private String buildPaymentSuccessEmail(Order order) {
        return String.format("""
            <div style="font-family: Arial, sans-serif;">
                <h2 style="color: #27ae60;">Thanh toán thành công!</h2>
                <p>Chúng tôi đã nhận được tiền cho đơn hàng <strong>%s</strong>.</p>
                <p>Trạng thái đơn hàng: <strong>Đã giao (DELIVERED)</strong></p>
                <p>Cảm ơn bạn!</p>
            </div>
            """, order.getOrderId());
    }

    private String extractOrderId(String content) {
        if (content == null || content.isBlank()) {
            throw new RuntimeException("Nội dung giao dịch trống");
        }

        String normalized = content.trim().toUpperCase();
        if (normalized.startsWith("SEVQR")) {
            String[] parts = normalized.split("\\s+");
            if (parts.length >= 2) {
                return parts[1];
            }
        }

        Pattern pattern = Pattern.compile("O\\d{3,}");
        Matcher matcher = pattern.matcher(normalized);
        if (matcher.find()) {
            return matcher.group();
        }

        throw new RuntimeException("Không xác định được mã đơn từ nội dung: " + content);
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

    private String buildCodSuccessEmail(Order order) {
        return String.format("""
            <div style="font-family: Arial, sans-serif;">
                <h2 style="color: #27ae60;">Mua hàng thành công!</h2>
                <p>Đơn hàng <strong>%s</strong> đã được thanh toán bằng tiền mặt.</p>
                <p>Trạng thái: <strong>Đã giao hàng (DELIVERED)</strong></p>
                <p>Tổng tiền: <strong>%,.0f VNĐ</strong></p>
                <p>Cảm ơn bạn đã mua sắm tại Petopia!</p>
            </div>
            """, order.getOrderId(), order.getTotalAmount());
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
            // Nội dung CK: "SEVQR [Mã Đơn]"
            String content = "SEVQR " + order.getOrderId();
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

    // --- Delivery helpers ---
    private void createInitialDeliveryForOrder(Order order) {
        // Nếu đã có delivery rồi thì không tạo lại
        if (order.getDelivery() != null) {
            return;
        }

        Delivery delivery = new Delivery();
        delivery.setDeliveryId(generateDeliveryId());
        delivery.setOrder(order);
        delivery.setShippingMethod(ShippingMethod.STANDARD);
        delivery.setShippingFee(order.getShippingFee() != null ? order.getShippingFee() : 0.0);
        delivery.setDeliveryStatus(DeliveryStatus.PREPARING);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        DeliveryHistory history = new DeliveryHistory();
        history.setHistoryId(generateDeliveryHistoryId());
        history.setDelivery(savedDelivery);
        history.setStatus(DeliveryStatus.PREPARING);
        history.setDescription("Đơn hàng đang được chuẩn bị tại kho");
        history.setLocation(null);
        deliveryHistoryRepository.save(history);
    }

    private String generateDeliveryId() {
        String lastId = deliveryRepository
                .findAll(PageRequest.of(0, 1, Sort.by("deliveryId").descending()))
                .stream()
                .findFirst()
                .map(Delivery::getDeliveryId)
                .orElse("D000");
        try {
            int num = Integer.parseInt(lastId.substring(1));
            return String.format("D%03d", num + 1);
        } catch (Exception e) {
            return "D" + System.currentTimeMillis();
        }
    }

    private String generateDeliveryHistoryId() {
        String lastId = deliveryHistoryRepository
                .findAll(PageRequest.of(0, 1, Sort.by("historyId").descending()))
                .stream()
                .findFirst()
                .map(DeliveryHistory::getHistoryId)
                .orElse("DH000");
        try {
            int num = Integer.parseInt(lastId.substring(2));
            return String.format("DH%03d", num + 1);
        } catch (Exception e) {
            return "DH" + System.currentTimeMillis();
        }
    }


}