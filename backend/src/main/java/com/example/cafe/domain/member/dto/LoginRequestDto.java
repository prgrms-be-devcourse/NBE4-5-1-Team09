package com.example.cafe.domain.member.dto;

import com.example.cafe.global.constant.ErrorMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    @Email(message = ErrorMessages.INVALID_EMAIL_FORMAT)
    private String email;

    @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
    private String password;
}
