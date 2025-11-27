package com.pet.modal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VaccineBatchCreateRequestDTO {
    @NotEmpty(message = "Phải chọn ít nhất một thú cưng")
    private List<String> petIds;

//    @NotBlank(message = "User ID không được để trống")
//    private String userId;

    // THAY ĐỔI: Nhận Start & End thay vì List<Date>
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    @NotBlank(message = "Tên vắc xin không được để trống")
    private String vaccineName;

    private String vaccineType;
    private String description;
    private String note;
}