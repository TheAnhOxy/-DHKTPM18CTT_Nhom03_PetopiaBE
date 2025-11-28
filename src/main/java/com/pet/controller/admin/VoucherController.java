package com.pet.controller.admin;

import com.pet.modal.request.ApplyVoucherRequestDTO;
import com.pet.modal.request.VoucherRequestDTO;
import com.pet.modal.request.VoucherSearchRequestDTO;
import com.pet.modal.response.PageResponse;
import com.pet.modal.response.PromotionResponseDTO;
import com.pet.modal.response.VoucherResponseDTO;
import com.pet.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/vouchers")
public class VoucherController {
    @Autowired
    private VoucherService voucherService;

    @GetMapping("/{voucherCode}")
    public ResponseEntity<VoucherResponseDTO> getVoucher(@PathVariable String voucherCode){
        VoucherResponseDTO voucher = voucherService.getVoucherByCode(voucherCode);
        if (voucher == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(voucher);
    }

    @GetMapping("/list")
    public ResponseEntity<PageResponse<VoucherResponseDTO>> getAllVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(voucherService.getAllVouchers(page, size));
    }

    @PostMapping
    public ResponseEntity<VoucherResponseDTO> addOrUpdateVoucher(@RequestBody VoucherRequestDTO request){
        request.validate();
        VoucherResponseDTO voucher = voucherService.addOrUpdateVoucher(request);
        if (request.getVoucherId() == null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(voucher);
        }
        return ResponseEntity.ok(voucher);
    }

    @PutMapping("/inactive/{voucherId}")
    public ResponseEntity<VoucherResponseDTO> inactiveVoucher(@PathVariable String voucherId){
        VoucherResponseDTO voucher = voucherService.inactiveVoucher(voucherId);
        if (voucher == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(voucher);
    }

    @PostMapping("/apply")
    public ResponseEntity<VoucherResponseDTO> applyVoucher(@RequestBody ApplyVoucherRequestDTO request){
        request.validate();
        VoucherResponseDTO voucher = voucherService.applyVoucher(request);
        if (voucher == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.ok(voucher);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<VoucherResponseDTO>> searchVouchers(
            @ModelAttribute VoucherSearchRequestDTO request) {

        if (request == null) {
            request = new VoucherSearchRequestDTO();
        }
        return ResponseEntity.ok(voucherService.searchVouchers(request));
    }

}
