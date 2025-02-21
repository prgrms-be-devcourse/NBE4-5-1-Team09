package com.example.cafe.domain.member.dto;

import com.example.cafe.global.constant.ErrorMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetConfirmRequestDto {
    @NotBlank(message = ErrorMessages.EMAIL_REQUIRED)
    @Email(message = ErrorMessages.INVALID_EMAIL_FORMAT)
    private String email;

    @NotBlank(message = ErrorMessages.RESET_CODE_REQUIRED)
    private String resetCode;

    @NotBlank(message = ErrorMessages.NEW_PASSWORD_REQUIRED)
    private String newPassword;
}
