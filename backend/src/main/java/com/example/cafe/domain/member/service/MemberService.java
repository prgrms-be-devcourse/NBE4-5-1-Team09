package com.example.cafe.domain.member.service;

import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    @Value("${custom.admin.admin-code}")
    private String secretAdminCode;

    // 일반 회원 가입
    public Member join(String email, String password, String address) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수 항목입니다.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수 항목입니다.");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("주소는 필수 항목입니다.");
        }
        Optional<Member> existing = memberRepository.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(password);
        Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
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
        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        String encodedPassword = passwordEncoder.encode(password);
        Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
                .address(address)
                .authority("ADMIN")
                .build();
        return memberRepository.save(member);
    }

    // 일반 회원 로그인
    public String login(String email, String rawPassword) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return authTokenService.genAccessToken(member);
    }

    // 관리자 로그인: 일반 로그인 후, 추가로 권한 확인
    public String loginAdmin(String email, String rawPassword) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (!"ADMIN".equalsIgnoreCase(member.getAuthority())) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }
        return authTokenService.genAccessToken(member);
    }
}
