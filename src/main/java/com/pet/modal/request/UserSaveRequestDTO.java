package com.pet.modal.request;

import com.pet.enums.UserRole;
import lombok.Data;
import java.util.List;
@Data
public class UserSaveRequestDTO {
    private String userId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String avatar;

    private String password;

    private UserRole role;
    private Boolean isActive;

    private List<AddressRequestDTO> addresses;
}