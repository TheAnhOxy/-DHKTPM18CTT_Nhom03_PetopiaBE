package com.pet.converter;

import com.pet.config.ModelMapperConfig;
import com.pet.entity.Category;
import com.pet.entity.Promotion;
import com.pet.entity.Voucher;
import com.pet.enums.PromotionVoucherStatus;
import com.pet.exception.ResourceNotFoundException;
import com.pet.modal.request.PromotionRequestDTO;
import com.pet.modal.request.VoucherRequestDTO;
import com.pet.modal.response.PromotionResponseDTO;
import com.pet.modal.response.VoucherResponseDTO;
import com.pet.repository.VoucherRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class VoucherConverter {
    @Autowired
    private ModelMapperConfig modelMapper;
    @Autowired
    private VoucherRepository voucherRepository;

    public VoucherResponseDTO mapToDTO(Voucher voucher) {
        return modelMapper.getModelMapper().map(voucher, VoucherResponseDTO.class);
    }

    public Voucher mapToEntity(VoucherRequestDTO requestDTO, Voucher voucher) {
        modelMapper.getModelMapper().map(requestDTO, voucher);

        if(voucher.getVoucherId() == null){
            voucher.setVoucherId(generateVoucherId());
            voucher.setStatus(PromotionVoucherStatus.ACTIVE);
            voucher.setUsedCount(0);
        } else {
            if (requestDTO.getStatus() != null &&
                    ("ACTIVE".equalsIgnoreCase(requestDTO.getStatus()) || "INACTIVE".equalsIgnoreCase(requestDTO.getStatus()))) {
                voucher.setStatus(PromotionVoucherStatus.valueOf(requestDTO.getStatus().toUpperCase()));
            }
            // Nếu frontend không gửi status → giữ nguyên status cũ
        }
        return voucher;
    }

    public void validateVoucher(Voucher voucher, Double orderAmount) {
        LocalDate now = LocalDate.now();
        if (voucher.getStatus() != PromotionVoucherStatus.ACTIVE) {
            throw new RuntimeException("Voucher is not active");
        }
        if (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) {
            throw new RuntimeException("Voucher is not yet valid");
        }
        if (voucher.getEndDate() != null && voucher.getEndDate().isBefore(now)) {
            throw new RuntimeException("Voucher has expired");
        }
        if (orderAmount != null && voucher.getMinOrderAmount() != null && orderAmount < voucher.getMinOrderAmount()) {
            throw new RuntimeException("Order amount does not meet the minimum requirement for this voucher");
        }
        if (voucher.getMaxUsage() != null && voucher.getUsedCount() >= voucher.getMaxUsage()) {
            throw new RuntimeException("Voucher usage limit has been reached");
        }
    }

    private String generateVoucherId() {
        long count = voucherRepository.count() + 1;
        return String.format("V%03d", count);
    }
}
