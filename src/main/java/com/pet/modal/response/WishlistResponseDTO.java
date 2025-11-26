package com.pet.modal.response;

import com.pet.enums.PetStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WishlistResponseDTO {
    private String wishlistId;
    private LocalDateTime addedAt;
    private String petId;
    private String petName;
    private Double petPrice;
    private String petImage;
    private PetStatus petStatus; // Để hiện chữ "Hết hàng" nếu cần
}