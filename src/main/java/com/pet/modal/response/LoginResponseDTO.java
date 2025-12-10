package com.pet.modal.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pet.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
//    @JsonProperty("access_token")
    private String accessToken;

//    @JsonProperty("refresh_token")
    private String refreshToken;
    private UserResponseDTO user;
}