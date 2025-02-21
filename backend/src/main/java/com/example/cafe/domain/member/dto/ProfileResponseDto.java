package com.example.cafe.domain.member.dto;

import lombok.Data;

@Data
public class ProfileResponseDto {
    private String email;
    private String address;
    private String authority;
}