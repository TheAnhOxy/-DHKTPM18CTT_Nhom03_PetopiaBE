package com.pet.modal.request;

import lombok.Data;

@Data
public class UserUpdateRequestDTO {
    private String fullName;
    private String email;
    private String avatar;
    private String phoneNumber;

}