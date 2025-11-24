package com.pet.modal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VaccineBatchCreateRequestDTO {
    @NotEmpty(message = "Phải chọn ít nhất một thú cưng")
    private List<String> petIds;

    @NotBlank(message = "User ID không được để trống")
    private String userId;

    @NotEmpty(message = "Phải chọn ít nhất một ngày tiêm")
    private List<LocalDateTime> scheduledDates;

    @NotBlank(message = "Tên vắc xin không được để trống")
    private String vaccineName;

    private String vaccineType;
    private String description;
    private String note;
}