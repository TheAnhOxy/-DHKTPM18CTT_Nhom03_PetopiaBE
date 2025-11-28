package com.pet.modal.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SePayWebhookDTO {
    private String gateway;
    private String transferType;
    private String description;
    private Double accumulated;
    private Long id;

    @JsonProperty("transactionDate")
    private String transactionDate;

    @JsonProperty("accountNumber")
    private String accountNo;

    @JsonProperty("subAccount")
    private String subAccount;

    @JsonProperty("transferAmount")
    private Double transferAmount; // Số tiền khách chuyển

    @JsonProperty("content")
    private String transferContent; // Nội dung: "SEVQR O012"

    @JsonProperty("referenceCode")
    private String referenceCode;

    @JsonIgnore
    public String resolveTransferContent() {
        if (transferContent != null && !transferContent.isBlank()) {
            return transferContent;
        }
        if (description != null && !description.isBlank()) {
            return description;
        }
        return "";
    }
}