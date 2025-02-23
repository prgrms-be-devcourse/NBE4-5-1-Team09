package com.example.cafe.domain.member.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProfileDto {
    private Long id;
    private String email;
    private String address;
    private String authority;
}
