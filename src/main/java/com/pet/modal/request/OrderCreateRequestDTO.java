package com.pet.modal.request;

import com.pet.enums.PaymentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderCreateRequestDTO {

    // --- Option 1: Chọn địa chỉ có sẵn ---
    private String addressId;

    // --- Option 2: Nhập địa chỉ mới (Nếu addressId null thì lấy cái này) ---
    private String newStreet;
    private String newWard;
    private String newDistrict;
    private String newProvince;

    // --- Thông tin người nhận (Form UI) ---
    @NotNull(message = "Số điện thoại người nhận là bắt buộc")
    private String phoneNumber; // Lưu vào bảng Order

    // Tên người nhận (Lưu ý: Entity Order hiện tại chưa có cột recipientName,
    // nên tạm thời mình sẽ chỉ dùng để update thông tin user hoặc log,
    // hoặc bạn cần thêm cột này vào bảng Order nếu muốn lưu riêng)
    private String recipientName;

    @NotEmpty(message = "Giỏ hàng không được để trống")
    private List<OrderItemRequestDTO> items;

    private String note;

    @NotNull(message = "Phương thức thanh toán là bắt buộc")
    private PaymentMethod paymentMethod; // COD hoặc BANK_TRANSFER
}