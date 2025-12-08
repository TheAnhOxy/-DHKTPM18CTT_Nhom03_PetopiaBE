package com.pet.modal.request;

import lombok.Data;

@Data
public class ChangePasswordRequestDTO {
    private String userId;
    private String oldPassword;
    private String newPassword;
}
