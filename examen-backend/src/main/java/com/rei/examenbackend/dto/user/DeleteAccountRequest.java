package com.rei.examenbackend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteAccountRequest {
    @NotBlank
    private String password;
}
