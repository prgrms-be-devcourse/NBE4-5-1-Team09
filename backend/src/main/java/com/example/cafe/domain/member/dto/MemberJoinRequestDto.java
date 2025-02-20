package com.example.cafe.domain.member.dto;

import com.example.cafe.global.constant.ErrorMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberJoinRequestDto {
    @NotBlank(message = ErrorMessages.EMAIL_REQUIRED)
    @Email(message = ErrorMessages.EMAIL_FORMAT_REQUIRED)
    private String email;

    @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
    private String password;

    @NotBlank(message = ErrorMessages.ADDRESS_REQUIRED)
    private String address;
}
