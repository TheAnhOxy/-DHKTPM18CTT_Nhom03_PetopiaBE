package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAttributesDTO {

    // ===== PRIMARY INTENT =====
    private String domain;           // pet | service | article | voucher | order | general
    private String intent;           // search | filter | sort | ask_price | ask_info | ask_list | ask_detail | track_order | general
    private Double confidence;       // 0.0 → 1.0

    // ===== KEYWORD =====
    private String keyword;
    private List<String> keywords;

    // ===== PRICE FILTER =====
    private Double minPrice;
    private Double maxPrice;

    // ===== PAGINATION =====
    private Integer limit;
    private Integer page;
    private Integer pageSize;

    // ===== SORTING =====
    private String sortBy;           // PRICE | DATE | POPULARITY | RELEVANCE
    private String sortDirection;    // ASC | DESC

    // ============================
    //       PET SPECIFIC
    // ============================
    private String breed;
    private String category;         // dog | cat | poodle | pug...
    private String size;             // small | medium | large
    private String furType;          // long | short | none
    private String color;
    private String gender;           // male | female
    private Integer minAge;
    private Integer maxAge;
    private Boolean availableOnly;
    private Double minVectorScore;

    // ============================
    //       SERVICE SPECIFIC
    // ============================
    private String serviceType;      // grooming | training | spa...
    private Integer duration;

    // ============================
    //       ARTICLE SPECIFIC
    // ============================
    private String topic;
    private String author;
    private Boolean trending;

    // ============================
    //       VOUCHER SPECIFIC
    // ============================
    private String voucherCode;
    private String voucherType;      // percentage | fixed_amount
    private Boolean onlyValid;

    // ============================
    //       ORDER SPECIFIC
    // ============================
    private String trackingId;
    private String orderId;
}
