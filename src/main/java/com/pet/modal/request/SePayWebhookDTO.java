package com.pet.modal.request;
import lombok.Data;

@Data
public class SePayWebhookDTO {
    private String transactionDate;
    private String accountNo;
    private String subAccount;
    private Double transferAmount; // Số tiền khách chuyển
    private String transferContent; // Nội dung: "THANHTOAN OR012"
    private String referenceCode;
}