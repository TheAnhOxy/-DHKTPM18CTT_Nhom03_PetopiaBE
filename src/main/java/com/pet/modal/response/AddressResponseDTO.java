package com.pet.modal.response;

import lombok.Data;

@Data
public class AddressResponseDTO {
    private String addressId;
    private String street;
    private String ward;
    private String district;
    private String province;
    private String country;
    private Boolean isDefault;
}