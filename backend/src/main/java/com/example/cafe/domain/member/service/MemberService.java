package com.example.cafe.domain.member.service;

import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 일반 회원 가입
    public Member join(String email, String password, String address) {

        Member member = Member.builder()
                .email(email)
                .password(password)
                .address(address)
                .authority("USER")
                .build();

        return memberRepository.save(member);
    }
}
