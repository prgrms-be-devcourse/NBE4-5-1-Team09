package com.example.cafe.domain.member.dto;

import com.example.cafe.global.constant.ErrorMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateRequestDto {
    @NotBlank(message = ErrorMessages.ADDRESS_REQUIRED)
    private String address;
}