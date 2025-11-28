package com.pet.service;

import com.pet.modal.request.ApplyVoucherRequestDTO;
import com.pet.modal.request.VoucherRequestDTO;
import com.pet.modal.request.VoucherSearchRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.VoucherResponseDTO;

public interface VoucherService {
    VoucherResponseDTO getVoucherByCode(String voucherCode);
    PageResponse<VoucherResponseDTO> getAllVouchers(int page, int size);
    VoucherResponseDTO addOrUpdateVoucher(VoucherRequestDTO request);
    VoucherResponseDTO inactiveVoucher(String voucherId);
    VoucherResponseDTO applyVoucher(ApplyVoucherRequestDTO request);

    PageResponse<VoucherResponseDTO> searchVouchers(VoucherSearchRequestDTO request);
}
