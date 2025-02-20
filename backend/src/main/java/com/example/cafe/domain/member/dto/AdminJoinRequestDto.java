package com.example.cafe.domain.member.dto;

import com.example.cafe.global.constant.ErrorMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AdminJoinRequestDto {
    @NotBlank(message = ErrorMessages.EMAIL_REQUIRED)
    @Email(message = ErrorMessages.INVALID_EMAIL_FORMAT)
    private String email;

    @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
    private String password;

    @NotBlank(message = ErrorMessages.ADDRESS_REQUIRED)
    private String address;

    @NotBlank(message = ErrorMessages.ADMIN_CODE_REQUIRED)
    private String adminCode;
}
