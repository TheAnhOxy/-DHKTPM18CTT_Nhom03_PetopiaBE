package com.pet.modal.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TopUserDTO {
    private String userId;
    private String fullName;
    private String email;
    private String avatar;
    private Double totalSpent; // Tổng tiền đã chi
    private Long totalOrders;  // Tổng số đơn mua thành công

    // Constructor cho JPQL
    public TopUserDTO(String userId, String fullName, String email, String avatar, Double totalSpent, Long totalOrders) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.avatar = avatar;
        this.totalSpent = totalSpent;
        this.totalOrders = totalOrders;
    }
}