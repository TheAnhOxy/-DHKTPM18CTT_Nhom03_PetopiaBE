package com.pet.converter;

import com.pet.enums.VaccineStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class VaccineStatusConverter implements AttributeConverter<VaccineStatus, String> {

    @Override
    public String convertToDatabaseColumn(VaccineStatus status) {
        if (status == null) {
            return null;
        }
        // Khi lưu xuống DB, ưu tiên lưu Label tiếng Việt
        return status.getLabel();
    }

    @Override
    public VaccineStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        // 1. Thử tìm theo Label tiếng Việt ("Chưa tiêm", "Đã tiêm")
        for (VaccineStatus status : VaccineStatus.values()) {
            if (status.getLabel().equalsIgnoreCase(dbData)) {
                return status;
            }
        }

        // 2. Nếu không thấy, thử tìm theo tên Enum ("CHUA_TIEM", "Da_TIEM")
        // (Để hỗ trợ dữ liệu cũ)
        try {
            return VaccineStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // 3. Nếu vẫn không khớp cái nào -> Trả về mặc định hoặc null để tránh lỗi 500
            System.err.println("Cảnh báo: Dữ liệu status không hợp lệ trong DB: " + dbData);
            return VaccineStatus.CHUA_TIEM; // Fallback an toàn
        }
    }
}