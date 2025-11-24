package com.pet.modal.response;

import com.pet.enums.UserRole;
import lombok.Data;
import java.util.List;

@Data
public class UserResponseDTO {
    private String userId;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String avatar;
    private UserRole role;
    private Boolean isActive;
    private List<AddressResponseDTO> addresses;
}