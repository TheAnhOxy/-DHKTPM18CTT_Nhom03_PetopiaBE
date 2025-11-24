package com.pet.modal.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequestDTO {
    @NotBlank(message = "Tên đường/số nhà không được để trống")
    private String street;

    private String ward;
    private String district;

    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String province;

    private String country = "Vietnam";

    private Boolean isDefault = false;
}