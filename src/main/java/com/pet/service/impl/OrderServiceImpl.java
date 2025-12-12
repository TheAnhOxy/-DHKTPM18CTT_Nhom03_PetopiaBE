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

    // Sequence dùng để sinh orderItemId an toàn trong 1 request
    private final AtomicInteger orderItemSequence = new AtomicInteger(0);

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
            order.setStatus(OrderStatus.SHIPPED); // Logic : COD coi như xong luôn? (Thường là Pending -> Shipping)
            order.setPaymentStatus(OrderPaymentStatus.PAID);
        } else {
            order.setStatus(OrderStatus.PENDING);
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

        //  Promotion (Theo mã khuyến mãi FE gửi lên)
        if (request.getPromotionCode() != null && !request.getPromotionCode().isBlank()) {
            Promotion promotion = promotionRepository.findByCode(request.getPromotionCode()).orElse(null);
            if (promotion != null && isValidPromotion(promotion, itemsTotal)) {
                double promoDiscount = calculatePromotionDiscount(promotion, itemsTotal);
                totalDiscount += promoDiscount;

                OrderPromotion op = new OrderPromotion();
                op.setOrderPromotionId(generateOrderPromotionId());
                op.setOrder(order);
                op.setPromotion(promotion);
                op.setDiscountApplied(promoDiscount);
                orderPromotions.add(op);

                // tăng lượt dùng
                promotion.setUsedCount((promotion.getUsedCount() != null ? promotion.getUsedCount() : 0) + 1);
                promotionRepository.save(promotion);
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

    private boolean isValidPromotion(Promotion p, double orderTotal) {
        if (p.getStatus() != PromotionVoucherStatus.ACTIVE) return false;
        if (p.getStartDate().isAfter(LocalDate.now()) || p.getEndDate().isBefore(LocalDate.now())) return false;
        if (p.getMinOrderAmount() != null && orderTotal < p.getMinOrderAmount()) return false;
        if (p.getMaxUsage() != null && p.getUsedCount() != null && p.getUsedCount() >= p.getMaxUsage()) return false;
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

    private double calculatePromotionDiscount(Promotion promo, double orderTotal) {
        if (promo.getPromotionType() == PromotionType.DISCOUNT && promo.getDiscountValue() != null && promo.getDiscountValue() <= 100) {
            // Giảm theo %
            return orderTotal * (promo.getDiscountValue() / 100.0);
        }
        // Các loại khác (FREESHIP, CASHBACK, BUNDLE hoặc DISCOUNT > 100): giảm cố định
        return promo.getDiscountValue() != null ? promo.getDiscountValue() : 0.0;
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
        // Lưu số tiền đã làm tròn (amount fixed) để tạo QR cố định
        payment.setAmount((double) Math.round(order.getTotalAmount()));
        payment.setPaymentMethod(method);
        payment.setPaymentDate(LocalDateTime.now());

        if (method == PaymentMethod.BANK_TRANSFER) {
            // BANK: Trạng thái PENDING, Tạo QR
            payment.setPaymentStatus(PaymentStatus.PENDING);
            String content = "SEVQR " + order.getOrderId();
            String qrUrl = sePayService.generateQrUrl(payment.getAmount(), content);
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

        // Nếu quá 10 phút -> Đánh dấu FAILED cho payment và order, không update trạng thái thành công
        if (createdTime.plusMinutes(10).isBefore(now)) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            Order timeoutOrder = payment.getOrder();
            if (timeoutOrder != null) {
                timeoutOrder.setPaymentStatus(OrderPaymentStatus.FAILED);
                orderRepository.save(timeoutOrder);
            }
            // Không throw exception để tránh rollback transaction
            return;
        }

        //  Kiểm tra số tiền: yêu cầu đúng số tiền đã fix trong QR (amount fixed)
        double requiredAmount = payment.getAmount(); // đã được làm tròn và cố định
        double transferredAmount = webhookData.getTransferAmount();

        // cho phép sai số rất nhỏ (0.5 VND) để tránh lỗi làm tròn từ phía ngân hàng
        double tolerance = 0.5;

        if (Math.abs(transferredAmount - requiredAmount) > tolerance) {
            // Chuyển sai số tiền (thiếu hoặc dư đáng kể): Đánh dấu FAILED
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            Order mismatchOrder = payment.getOrder();
            if (mismatchOrder != null) {
                mismatchOrder.setPaymentStatus(OrderPaymentStatus.FAILED);
                orderRepository.save(mismatchOrder);
            }
            // Không throw exception để tránh rollback transaction
            return;
        }

        //UPDATE TRẠNG THÁI THÀNH CÔNG
        // Update Payment
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        // Update Order -> DELIVERED & PAID
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentStatus(OrderPaymentStatus.PAID);
        orderRepository.save(order);

        //  Gửi mail xác nhận thanh toán thành công
        emailService.sendEmail(
                order.getUser().getEmail(),
                "[Petopia] Thanh toán thành công đơn #" + orderId,
                buildPaymentSuccessEmail(order, payment)
        );
    }

    private String buildPaymentSuccessEmail(Order order, Payment payment) {
        // Lấy thông tin địa chỉ
        String shippingAddress = "Chưa có thông tin";
        if (order.getAddress() != null) {
            Address addr = order.getAddress();
            shippingAddress = String.format("%s, %s, %s, %s",
                    addr.getStreet(), addr.getWard(), addr.getDistrict(), addr.getProvince());
        }

        // Lấy phương thức thanh toán
        String paymentMethodText = payment.getPaymentMethod() == PaymentMethod.BANK_TRANSFER
                ? "Chuyển khoản ngân hàng"
                : "Thanh toán khi nhận hàng (COD)";

        // Format ngày đặt hàng
        String orderDate = order.getCreatedAt() != null
                ? order.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "N/A";

        // Tính tổng tiền sản phẩm (trước khi trừ giảm giá và cộng phí ship)
        double itemsSubtotal = order.getTotalAmount()
                - (order.getShippingFee() != null ? order.getShippingFee() : 0.0)
                + (order.getDiscountAmount() != null ? order.getDiscountAmount() : 0.0);

        // Build danh sách sản phẩm
        StringBuilder itemsHtml = new StringBuilder();
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (OrderItem item : order.getOrderItems()) {
                String petName = item.getPet() != null ? item.getPet().getName() : "Sản phẩm";
                int quantity = item.getQuantity() != null ? item.getQuantity() : 1;
                double price = item.getPriceAtPurchase() != null ? item.getPriceAtPurchase() : 0.0;
                double itemTotal = price * quantity;

                itemsHtml.append(String.format("""
                    <tr style="border-bottom: 1px solid #e0e0e0;">
                        <td style="padding: 12px; vertical-align: top; word-wrap: break-word;">
                            <strong>%s</strong>
                        </td>
                        <td style="padding: 12px; text-align: center;">%d</td>
                        <td style="padding: 12px; text-align: right; word-wrap: break-word;">%,.0f VNĐ</td>
                        <td style="padding: 12px; text-align: right; word-wrap: break-word;"><strong>%,.0f VNĐ</strong></td>
                    </tr>
                    """, petName, quantity, price, itemTotal));
            }
        } else {
            itemsHtml.append("""
                <tr>
                    <td colspan="4" style="padding: 20px; text-align: center; color: #888;">
                        Không có sản phẩm
                    </td>
                </tr>
                """);
        }

        // Lấy tên khách hàng
        String customerName = order.getUser() != null ? order.getUser().getFullName() : "Quý khách";

        return String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f9f9f9; padding: 20px;">
                <!-- Auto Email Notice -->
                <div style="text-align: center; color: #888; font-size: 12px; margin-bottom: 10px; padding: 10px; background-color: #f0f0f0; border-radius: 5px;">
                    <p style="margin: 0;">⚠️ Đây là email tự động. Vui lòng không trả lời email này</p>
                </div>

                <!-- Header -->
                <div style="background: linear-gradient(135deg, #27ae60 0%%, #2ecc71 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 28px;">✓ Xác nhận thanh toán thành công</h1>
                </div>

                <!-- Main Content -->
                <div style="background-color: #ffffff; padding: 30px; border-radius: 0 0 10px 10px;">
                    <!-- Greeting -->
                    <div style="margin-bottom: 25px;">
                        <p style="font-size: 16px; color: #2c3e50; margin: 0 0 15px 0;">
                            Xin chào <strong>%s</strong>,
                        </p>
                        <p style="font-size: 15px; color: #555; margin: 0 0 15px 0; line-height: 1.6;">
                            Cảm ơn quý khách đã tin tưởng sử dụng dịch vụ của Petopia.
                        </p>
                        <p style="font-size: 15px; color: #555; margin: 0; line-height: 1.6;">
                            Petopia xác nhận quý khách đã thanh toán thành công đơn hàng thú cưng.
                        </p>
                    </div>

                    <!-- Security Warning -->
                    <div style="background-color: #fff3cd; border: 2px solid #ffc107; border-left: 5px solid #ff9800; padding: 15px; border-radius: 5px; margin-bottom: 25px;">
                        <p style="margin: 0; color: #856404; font-size: 14px; line-height: 1.6;">
                            <strong style="font-size: 16px;">⚠️ Cảnh báo:</strong> Petopia <strong>KHÔNG</strong> bao giờ yêu cầu quý khách truy cập liên kết lạ, cung cấp mã OTP ngân hàng hoặc chuyển tiền vào tài khoản không đứng tên "<strong>NGUYEN DUC HAU</strong>". Vui lòng chỉ sử dụng website Petopia để kiểm tra thông tin thú cưng và thanh toán.
                        </p>
                    </div>
                    <!-- Order Info -->
                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 25px;">
                        <h2 style="color: #2c3e50; margin-top: 0; font-size: 20px; border-bottom: 2px solid #27ae60; padding-bottom: 10px;">
                            Thông tin đơn hàng
                        </h2>
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; color: #555; width: 140px;"><strong>Mã đơn hàng:</strong></td>
                                <td style="padding: 8px 0; color: #2c3e50; font-size: 18px;"><strong>#%s</strong></td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #555;"><strong>Ngày đặt hàng:</strong></td>
                                <td style="padding: 8px 0; color: #2c3e50;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #555;"><strong>Trạng thái:</strong></td>
                                <td style="padding: 8px 0; color: #27ae60; font-weight: bold;">✓ Đang giao hàng (IN_TRAINTS)</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #555;"><strong>Phương thức thanh toán:</strong></td>
                                <td style="padding: 8px 0; color: #2c3e50;">%s</td>
                            </tr>
                        </table>
                    </div>

                    <!-- Products -->
                    <div style="margin-bottom: 25px;">
                        <h2 style="color: #2c3e50; margin-top: 0; font-size: 20px; border-bottom: 2px solid #27ae60; padding-bottom: 10px;">
                            Sản phẩm đã mua
                        </h2>
                        <table style="width: 100%%; border-collapse: collapse; background-color: #ffffff; word-wrap: break-word;">
                            <thead>
                                <tr style="background-color: #f8f9fa;">
                                    <th style="padding: 12px; text-align: left; border-bottom: 2px solid #e0e0e0; width: 35%%;">Tên sản phẩm</th>
                                    <th style="padding: 12px; text-align: center; border-bottom: 2px solid #e0e0e0; width: 15%%;">Số lượng</th>
                                    <th style="padding: 12px; text-align: right; border-bottom: 2px solid #e0e0e0; width: 25%%;">Đơn giá</th>
                                    <th style="padding: 12px; text-align: right; border-bottom: 2px solid #e0e0e0; width: 25%%;">Thành tiền</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                    </div>

                    <!-- Shipping Info -->
                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 25px;">
                        <h2 style="color: #2c3e50; margin-top: 0; font-size: 20px; border-bottom: 2px solid #27ae60; padding-bottom: 10px;">
                            Thông tin giao hàng
                        </h2>
                        <p style="margin: 8px 0; color: #555;"><strong>Người nhận:</strong> <span style="color: #2c3e50;">%s</span></p>
                        <p style="margin: 8px 0; color: #555;"><strong>Số điện thoại:</strong> <span style="color: #2c3e50;">%s</span></p>
                        <p style="margin: 8px 0; color: #555;"><strong>Địa chỉ giao hàng:</strong></p>
                        <p style="margin: 8px 0 0 20px; color: #2c3e50; padding: 10px; background-color: #ffffff; border-left: 3px solid #27ae60; border-radius: 4px;">
                            %s
                        </p>
                    </div>

                    <!-- Payment Summary -->
                    <div style="background-color: #f8f9fa; padding: 16px; border-radius: 8px; margin-bottom: 25px;">
                        <h2 style="color: #2c3e50; margin-top: 0; font-size: 16px; border-bottom: 2px solid #27ae60; padding-bottom: 8px;">
                            Tổng kết thanh toán
                        </h2>
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 6px 0; color: #555; width: 60%%; font-size: 12px;">Tạm tính:</td>
                                <td style="padding: 6px 0; text-align: right; color: #2c3e50; width: 40%%; font-size: 12px; word-wrap: break-word; word-break: break-word;">%,.0f VNĐ</td>
                            </tr>
                            <tr>
                                <td style="padding: 6px 0; color: #555; font-size: 12px;">Phí vận chuyển:</td>
                                <td style="padding: 6px 0; text-align: right; color: #2c3e50; font-size: 12px; word-wrap: break-word; word-break: break-word;">%,.0f VNĐ</td>
                            </tr>
                            %s
                            <tr style="border-top: 2px solid #27ae60; margin-top: 8px;">
                                <td style="padding: 10px 0; font-size: 14px; color: #2c3e50;"><strong>Tổng thanh toán:</strong></td>
                                <td style="padding: 10px 0; text-align: right; font-size: 16px; color: #27ae60; font-weight: bold; word-wrap: break-word; word-break: break-word;">%,.0f VNĐ</td>
                            </tr>
                        </table>
                    </div>

                    <!-- Note -->
                    <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; border-radius: 4px; margin-bottom: 25px;">
                        <p style="margin: 0; color: #856404; font-size: 14px;">
                            <strong>📦 Lưu ý:</strong> Đơn hàng của bạn đã được xác nhận thanh toán thành công và đang trong quá trình giao hàng. 
                            Chúng tôi sẽ liên hệ với bạn sớm nhất có thể.
                        </p>
                    </div>

                    <!-- Footer -->
                    <div style="text-align: center; padding-top: 20px; border-top: 1px solid #e0e0e0; color: #888; font-size: 14px;">
                        <p style="margin: 5px 0;">Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của Petopia!</p>
                        <p style="margin: 5px 0;">Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ hotline: <strong>1900-xxxx</strong></p>
                    </div>
                </div>
            </div>
            """,
                customerName,
                order.getOrderId(),
                orderDate,
                paymentMethodText,
                itemsHtml.toString(),
                order.getUser() != null ? order.getUser().getFullName() : "N/A",
                order.getPhoneNumber() != null ? order.getPhoneNumber() : "N/A",
                shippingAddress,
                itemsSubtotal,
                order.getShippingFee() != null ? order.getShippingFee() : 0.0,
                order.getDiscountAmount() != null && order.getDiscountAmount() > 0
                        ? String.format("""
                    <tr>
                        <td style="padding: 10px 0; color: #555;">Giảm giá:</td>
                        <td style="padding: 10px 0; text-align: right; color: #e74c3c; word-wrap: break-word;">-%,.0f VNĐ</td>
                    </tr>
                    """, order.getDiscountAmount())
                        : "",
                order.getTotalAmount()
        );
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
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #2c3e50;">Cảm ơn bạn đã đặt hàng!</h2>
                <p>Đơn hàng <strong>%s</strong> đang chờ thanh toán.</p>
                <p>Tổng tiền: <strong style="font-size: 14px; color: #e74c3c; word-wrap: break-word; word-break: break-word; display: inline-block; max-width: 100%%;">%,.0f VNĐ</strong></p>
                
                <div style="border: 2px dashed #3498db; padding: 15px; text-align: center; margin: 20px 0;">
                    <p>Quét mã QR để thanh toán ngay:</p>
                    <img src="%s" alt="QR SePay" width="250" style="max-width: 100%%; height: auto;" />
                    <p style="margin-top: 10px; word-wrap: break-word;">Hoặc chuyển khoản với nội dung: <strong style="background: #f1c40f; padding: 5px; word-wrap: break-word; display: inline-block; max-width: 100%%;">%s</strong></p>
                </div>
                <p>Đơn hàng sẽ được xử lý ngay sau khi chúng tôi nhận được tiền.</p>
            </div>
            """, order.getOrderId(), order.getTotalAmount(), qrUrl, content);
    }

    private String buildCodSuccessEmail(Order order) {
        // Lấy thông tin địa chỉ
        String shippingAddress = "Chưa có thông tin";
        if (order.getAddress() != null) {
            Address addr = order.getAddress();
            shippingAddress = String.format("%s, %s, %s, %s",
                    addr.getStreet(), addr.getWard(), addr.getDistrict(), addr.getProvince());
        }

        // Format ngày đặt hàng
        String orderDate = order.getCreatedAt() != null
                ? order.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "N/A";

        // Tính tổng tiền sản phẩm (trước khi trừ giảm giá và cộng phí ship)
        double itemsSubtotal = order.getTotalAmount()
                - (order.getShippingFee() != null ? order.getShippingFee() : 0.0)
                + (order.getDiscountAmount() != null ? order.getDiscountAmount() : 0.0);

        // Build danh sách sản phẩm
        StringBuilder itemsHtml = new StringBuilder();
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (OrderItem item : order.getOrderItems()) {
                String petName = item.getPet() != null ? item.getPet().getName() : "Sản phẩm";
                int quantity = item.getQuantity() != null ? item.getQuantity() : 1;
                double price = item.getPriceAtPurchase() != null ? item.getPriceAtPurchase() : 0.0;
                double itemTotal = price * quantity;

                itemsHtml.append(String.format("""
                    <tr style="border-bottom: 1px solid #e0e0e0;">
                        <td style="padding: 12px; vertical-align: top; word-wrap: break-word;">
                            <strong>%s</strong>
                        </td>
                        <td style="padding: 12px; text-align: center;">%d</td>
                        <td style="padding: 12px; text-align: right; word-wrap: break-word;">%,.0f VNĐ</td>
                        <td style="padding: 12px; text-align: right; word-wrap: break-word;"><strong>%,.0f VNĐ</strong></td>
                    </tr>
                    """, petName, quantity, price, itemTotal));
            }
        } else {
            itemsHtml.append("""
                <tr>
                    <td colspan="4" style="padding: 20px; text-align: center; color: #888;">
                        Không có sản phẩm
                    </td>
                </tr>
                """);
        }

        // Lấy tên khách hàng
        String customerName = order.getUser() != null ? order.getUser().getFullName() : "Quý khách";

        return String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #f9f9f9; padding: 20px;">
                <!-- Auto Email Notice -->
                <div style="text-align: center; color: #888; font-size: 12px; margin-bottom: 10px; padding: 10px; background-color: #f0f0f0; border-radius: 5px;">
                    <p style="margin: 0;">⚠️ Đây là email tự động. Vui lòng không trả lời email này</p>
                </div>

                <!-- Header -->
                <div style="background: linear-gradient(135deg, #27ae60 0%%, #2ecc71 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 28px;">✓ Đơn hàng hoàn tất</h1>
                </div>

                <!-- Main Content -->
                <div style="background-color: #ffffff; padding: 30px; border-radius: 0 0 10px 10px;">
                    <!-- Greeting -->
                    <div style="margin-bottom: 25px;">
                        <p style="font-size: 16px; color: #2c3e50; margin: 0 0 15px 0;">
                            Xin chào <strong>%s</strong>,
                        </p>
                        <p style="font-size: 15px; color: #555; margin: 0 0 15px 0; line-height: 1.6;">
                            Cảm ơn quý khách đã tin tưởng sử dụng dịch vụ của Petopia.
                        </p>
                        <p style="font-size: 15px; color: #555; margin: 0; line-height: 1.6;">
                            Petopia xác nhận quý khách đã thanh toán thành công đơn hàng thú cưng bằng tiền mặt (COD).
                        </p>
                    </div>

                    <!-- Security Warning -->
                    <div style="background-color: #fff3cd; border: 2px solid #ffc107; border-left: 5px solid #ff9800; padding: 15px; border-radius: 5px; margin-bottom: 25px;">
                        <p style="margin: 0; color: #856404; font-size: 14px; line-height: 1.6;">
                            <strong style="font-size: 16px;">⚠️ Cảnh báo:</strong> Petopia <strong>KHÔNG</strong> bao giờ yêu cầu quý khách truy cập liên kết lạ, cung cấp mã OTP ngân hàng hoặc chuyển tiền vào tài khoản không đứng tên "<strong>NGUYEN DUC HAU</strong>". Vui lòng chỉ sử dụng website Petopia để kiểm tra thông tin thú cưng và thanh toán.
                        </p>
                    </div>

                    <!-- Order Info -->
                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 25px;">
                        <h2 style="color: #2c3e50; margin-top: 0; font-size: 20px; border-bottom: 2px solid #27ae60; padding-bottom: 10px;">
                            Thông tin đơn hàng
                        </h2>
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; color: #555; width: 140px;"><strong>Mã đơn hàng:</strong></td>
                                <td style="padding: 8px 0; color: #2c3e50; font-size: 18px;"><strong>#%s</strong></td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #555;"><strong>Ngày đặt hàng:</strong></td>
                                <td style="padding: 8px 0; color: #2c3e50;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #555;"><strong>Trạng thái:</strong></td>
                                <td style="padding: 8px 0; color: #27ae60; font-weight: bold;">✓ Đã giao hàng (DELIVERED)</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; color: #555;"><strong>Phương thức thanh toán:</strong></td>
                                <td style="padding: 8px 0; color: #2c3e50;">Thanh toán khi nhận hàng (COD)</td>
                            </tr>
                        </table>
                    </div>

                    <!-- Products -->
                    <div style="margin-bottom: 25px;">
                        <h2 style="color: #2c3e50; margin-top: 0; font-size: 20px; border-bottom: 2px solid #27ae60; padding-bottom: 10px;">
                            Sản phẩm đã mua
                        </h2>
                        <table style="width: 100%%; border-collapse: collapse; background-color: #ffffff; word-wrap: break-word;">
                            <thead>
                                <tr style="background-color: #f8f9fa;">
                                    <th style="padding: 12px; text-align: left; border-bottom: 2px solid #e0e0e0; width: 35%%;">Tên sản phẩm</th>
                                    <th style="padding: 12px; text-align: center; border-bottom: 2px solid #e0e0e0; width: 15%%;">Số lượng</th>
                                    <th style="padding: 12px; text-align: right; border-bottom: 2px solid #e0e0e0; width: 25%%;">Đơn giá</th>
                                    <th style="padding: 12px; text-align: right; border-bottom: 2px solid #e0e0e0; width: 25%%;">Thành tiền</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                    </div>

                    <!-- Shipping Info -->
                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 25px;">
                        <h2 style="color: #2c3e50; margin-top: 0; font-size: 20px; border-bottom: 2px solid #27ae60; padding-bottom: 10px;">
                            Thông tin giao hàng
                        </h2>
                        <p style="margin: 8px 0; color: #555;"><strong>Người nhận:</strong> <span style="color: #2c3e50;">%s</span></p>
                        <p style="margin: 8px 0; color: #555;"><strong>Số điện thoại:</strong> <span style="color: #2c3e50;">%s</span></p>
                        <p style="margin: 8px 0; color: #555;"><strong>Địa chỉ giao hàng:</strong></p>
                        <p style="margin: 8px 0 0 20px; color: #2c3e50; padding: 10px; background-color: #ffffff; border-left: 3px solid #27ae60; border-radius: 4px;">
                            %s
                        </p>
                    </div>

                    <!-- Payment Summary -->
                    <div style="background-color: #f8f9fa; padding: 16px; border-radius: 8px; margin-bottom: 25px;">
                        <h2 style="color: #2c3e50; margin-top: 0; font-size: 16px; border-bottom: 2px solid #27ae60; padding-bottom: 8px;">
                            Tổng kết thanh toán
                        </h2>
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 6px 0; color: #555; width: 60%%; font-size: 12px;">Tạm tính:</td>
                                <td style="padding: 6px 0; text-align: right; color: #2c3e50; width: 40%%; font-size: 12px; word-wrap: break-word; word-break: break-word;">%,.0f VNĐ</td>
                            </tr>
                            <tr>
                                <td style="padding: 6px 0; color: #555; font-size: 12px;">Phí vận chuyển:</td>
                                <td style="padding: 6px 0; text-align: right; color: #2c3e50; font-size: 12px; word-wrap: break-word; word-break: break-word;">%,.0f VNĐ</td>
                            </tr>
                            %s
                            <tr style="border-top: 2px solid #27ae60; margin-top: 8px;">
                                <td style="padding: 10px 0; font-size: 14px; color: #2c3e50;"><strong>Tổng thanh toán:</strong></td>
                                <td style="padding: 10px 0; text-align: right; font-size: 16px; color: #27ae60; font-weight: bold; word-wrap: break-word; word-break: break-word;">%,.0f VNĐ</td>
                            </tr>
                        </table>
                    </div>

                    <!-- Note -->
                    <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; border-radius: 4px; margin-bottom: 25px;">
                        <p style="margin: 0; color: #856404; font-size: 14px;">
                            <strong>📦 Lưu ý:</strong> Đơn hàng của bạn đã được xác nhận thanh toán thành công và đang trong quá trình giao hàng. 
                            Chúng tôi sẽ liên hệ với bạn sớm nhất có thể.
                        </p>
                    </div>

                    <!-- Footer -->
                    <div style="text-align: center; padding-top: 20px; border-top: 1px solid #e0e0e0; color: #888; font-size: 14px;">
                        <p style="margin: 5px 0;">Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của Petopia!</p>
                        <p style="margin: 5px 0;">Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ hotline: <strong>1900-xxxx</strong></p>
                    </div>
                </div>
            </div>
            """,
                customerName,
                order.getOrderId(),
                orderDate,
                itemsHtml.toString(),
                order.getUser() != null ? order.getUser().getFullName() : "N/A",
                order.getPhoneNumber() != null ? order.getPhoneNumber() : "N/A",
                shippingAddress,
                itemsSubtotal,
                order.getShippingFee() != null ? order.getShippingFee() : 0.0,
                order.getDiscountAmount() != null && order.getDiscountAmount() > 0
                        ? String.format("""
                    <tr>
                        <td style="padding: 10px 0; color: #555;">Giảm giá:</td>
                        <td style="padding: 10px 0; text-align: right; color: #e74c3c; word-wrap: break-word;">-%,.0f VNĐ</td>
                    </tr>
                    """, order.getDiscountAmount())
                        : "",
                order.getTotalAmount()
        );
    }

    // Helper: Tạo Payment
    private void createPaymentRecord(Order order, PaymentMethod method) {
        Payment payment = new Payment();
        payment.setPaymentId(generatePaymentId()); // PMxxx
        payment.setOrder(order);
        // Lưu số tiền đã làm tròn (amount fixed) để tạo QR cố định
        payment.setAmount((double) Math.round(order.getTotalAmount()));
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());

        // Nếu là Chuyển khoản -> Tạo QR SePay
        if (method == PaymentMethod.BANK_TRANSFER) {
            // Nội dung CK: "SEVQR [Mã Đơn]"
            String content = "SEVQR " + order.getOrderId();
            String qrUrl = sePayService.generateQrUrl(payment.getAmount(), content);

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
        if (status == OrderStatus.DELIVERED) {
            order.setPaymentStatus(OrderPaymentStatus.PAID);
        }

        // --- BỔ SUNG: ĐỒNG BỘ SANG DELIVERY VÀ TẠO HISTORY ---
        // Lấy thông tin vận chuyển gắn với đơn hàng này
        Delivery delivery = deliveryRepository.findByOrder_OrderId(orderId).orElse(null);

        if (delivery != null) {
            DeliveryStatus newDeliveryStatus = null;
            String historyNote = "";

            // Map trạng thái từ Order sang Delivery
            switch (status) {
                case SHIPPED:
                    // Khi Admin bấm "Đã gửi hàng" -> Delivery chuyển sang "Đã xuất kho"
                    newDeliveryStatus = DeliveryStatus.IN_TRANSIT;
                    historyNote = "Đơn hàng đã được giao cho đơn vị vận chuyển";
                    break;

                case DELIVERED:
                    newDeliveryStatus = DeliveryStatus.DELIVERED;
                    delivery.setActualDeliveryDate(LocalDateTime.now());
                    historyNote = "Giao hàng thành công tới khách hàng";
                    break;

                case CANCELLED:
                    // Khi Admin bấm "Hủy" -> Delivery chuyển sang "Thất bại" hoặc "Hủy"
                    newDeliveryStatus = DeliveryStatus.FAILED;
                    historyNote = "Đơn hàng đã bị hủy";
                    break;

                default:
                    break;
            }

            // Nếu có sự thay đổi trạng thái Delivery tương ứng
            if (newDeliveryStatus != null && delivery.getDeliveryStatus() != newDeliveryStatus) {
                //  Cập nhật bảng Delivery
                delivery.setDeliveryStatus(newDeliveryStatus);
                deliveryRepository.save(delivery);

                // Tạo bản ghi lịch sử (DeliveryHistory)
                DeliveryHistory history = new DeliveryHistory();
                history.setHistoryId(generateDeliveryHistoryId()); // Sử dụng hàm sinh ID của bạn
                history.setDelivery(delivery);
                history.setStatus(newDeliveryStatus);
                history.setDescription(historyNote);
                history.setLocation("Hệ thống quản trị"); // Hoặc lấy địa chỉ kho nếu có
                history.setUpdatedAt(LocalDateTime.now()); // Entity bạn dùng @CreationTimestamp nên field này có thể tự sinh, hoặc set thủ công

                deliveryHistoryRepository.save(history);
            }
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
        if (orderItemSequence.get() == 0) {
            String lastId = orderItemRepository.findLastOrderItemId().orElse("OI000");
            try {
                int current = Integer.parseInt(lastId.substring(2));
                orderItemSequence.set(current);
            } catch (Exception e) {
                orderItemSequence.set(0);
            }
        }

        int next = orderItemSequence.incrementAndGet();
        return String.format("OI%03d", next);
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