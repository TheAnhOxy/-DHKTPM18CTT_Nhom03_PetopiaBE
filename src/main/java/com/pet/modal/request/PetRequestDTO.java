package com.pet.modal.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class PetRequestDTO {

    private String petId;

    @NotBlank(message = "Tên thú cưng không được để trống !!")
    private String name;

    private String description;

    @NotBlank(message = "Vui lòng chọn phân loại thú cưng")
    private String categoryId;

    @NotNull(message = "Tuổi không được để trống")
    @Min(value = 1, message = "Tuổi phải ít nhất là 1 tháng") // Sửa từ PositiveOrZero thành Min(1) nếu muốn bắt buộc nhập
    private Integer age;

    private String gender;

    @NotNull(message = "Giá bán không được để trống")
    @Min(value = 1000, message = "Giá bán phải ít nhất 1.000 VNĐ")
    private Double price;

    @PositiveOrZero(message = "Giá giảm không được âm")
    private Double discountPrice;

    private String healthStatus;
    private String vaccinationHistory;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 1, message = "Số lượng tồn kho phải ít nhất là 1")
    private Integer stockQuantity;

    private String status;
    private String videoUrl;

    @NotNull(message = "Cân nặng không được để trống")
    @Positive(message = "Cân nặng phải lớn hơn 0")
    private Double weight;

    @NotNull(message = "Chiều cao không được để trống")
    @Positive(message = "Chiều cao phải lớn hơn 0")
    private Double height;

    private String color;
    private String furType;

    private List<PetImageDTO> oldImages;
    private List<MultipartFile> files;
}