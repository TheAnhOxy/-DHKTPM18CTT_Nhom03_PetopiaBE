package com.pet.modal.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiIntentDTO {
    private String intent;

    private String keyword;

    private Double max_price;
    private Double min_price;

    private String furType;     // "curly", "long", "short", ...
    private String size;        // "small", "medium", "large"
    private String color;       // "white","black",...

    private String sortBy;       // "PRICE" | "DATE" | null
    private String sortDirection; // "ASC" | "DESC" | null

    private Boolean wantsBest;

    private String tracking_id;

    private Double confidence;
}
