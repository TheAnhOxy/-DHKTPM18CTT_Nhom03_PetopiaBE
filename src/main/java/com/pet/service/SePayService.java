package com.pet.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SePayService {

    @Value("${sepay.bank-code}") private String bankCode;
    @Value("${sepay.account-no}") private String accountNo;
    @Value("${sepay.template}") private String template;

    // Tạo Link ảnh QR Code
    public String generateQrUrl(double amount, String orderInfo) {
        // Format: https://qr.sepay.vn/img?acc={acc}&bank={bank}&amount={amount}&des={des}
        return String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%.0f&des=%s&template=%s",
                accountNo, bankCode, amount, orderInfo, template);
    }
}