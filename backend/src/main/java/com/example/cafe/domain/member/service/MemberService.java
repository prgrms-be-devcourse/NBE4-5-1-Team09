package com.example.cafe.domain.member.service;

import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@PropertySource(value = "classpath:application.yml")
public class MemberService {

    private final MemberRepository memberRepository;

    @Value("${custom.admin.admin-code}")
    private String secretAdminCode;

    // 일반 회원 가입
    public Member join(String email, String password, String address) {

        // 중복 이메일 체크
        Optional<Member> existing = memberRepository.findByEmail(email);
        if(existing.isPresent()){
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }


        Member member = Member.builder()
                .email(email)
                .password(password)
                .address(address)
                .authority("USER")
                .build();

        return memberRepository.save(member);
    }

    // 관리자 회원 가입
    public Member joinAdmin(String email, String password, String address, String adminCode) {

        if (!secretAdminCode.equals(adminCode)) {
            throw new IllegalArgumentException("관리자 인증 코드가 올바르지 않습니다.");
        }

        Optional<Member> existing = memberRepository.findByEmail(email);
        if(existing.isPresent()){
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        Member member = Member.builder()
                .email(email)
                .password(password)
                .address(address)
                .authority("ADMIN")
                .build();
        return memberRepository.save(member);
    }

    // 일반 회원 로그인
    public Member login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));
        if(!password.equals(member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    // 관리자 로그인
    public Member loginAdmin(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));
        if(!password.equals(member.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if(!"ADMIN".equalsIgnoreCase(member.getAuthority())){
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }
        return member;
    }
}
