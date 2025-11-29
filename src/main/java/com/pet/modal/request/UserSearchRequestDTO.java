package com.pet.modal.request;

import com.pet.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequestDTO {
    private String keyword;
    private String role;
    private Boolean isActive;
    private Integer page = 0;
    private Integer size = 10;
}
