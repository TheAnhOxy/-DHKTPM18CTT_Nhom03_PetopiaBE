package com.pet.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SePayService {

    @Value("${sepay.bank-code}") private String bankCode;
    @Value("${sepay.account-no}") private String accountNo;
    @Value("${sepay.template}") private String template;

    /**
     * Tạo link QR với số tiền cố định (amount fixed). Số tiền được làm tròn về VND (không decimal)
     * và embed vào QR để app ngân hàng khóa ô nhập tiền.
     */
    public String generateQrUrl(double amount, String orderInfo) {
        long fixedAmount = BigDecimal.valueOf(amount)
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        // amount là số nguyên, QR sepay sẽ khóa ô nhập tiền khi quét
        return String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%d&des=%s&template=%s",
                accountNo, bankCode, fixedAmount, orderInfo, template);
    }
}