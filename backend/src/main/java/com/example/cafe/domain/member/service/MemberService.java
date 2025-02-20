package com.example.cafe.domain.member.service;

import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.global.constant.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final EmailVerificationService emailVerificationService;

    @Value("${custom.admin.admin-code}")
    private String secretAdminCode;

    // 일반 회원 가입 (이메일 인증 코드 발송 로직 포함)
    @Transactional
    public Member join(String email, String password, String address) {
        log.info("일반 회원 가입 시도 :{}", email);
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.EMAIL_REQUIRED);
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.PASSWORD_REQUIRED);
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.ADDRESS_REQUIRED);
        }
        Optional<Member> existing = memberRepository.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalArgumentException(ErrorMessages.ALREADY_REGISTERED_EMAIL);
        }

        String encodedPassword = passwordEncoder.encode(password);
        Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
                .address(address)
                .authority("USER")
                .verified(false)
                .build();

        member = memberRepository.save(member);
        try {
            emailVerificationService.sendVerificationEmail(member);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", email, e);
            throw new RuntimeException("이메일 발송 실패: " + e.getMessage());
        }
        return member;
    }

    // 관리자 회원 가입
    @Transactional
    public Member joinAdmin(String email, String password, String address, String adminCode) {
        log.info("관리자 회원 가입 시도: {}", email);
        if (!secretAdminCode.equals(adminCode)) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_ADMIN_CODE);
        }
        Optional<Member> existing = memberRepository.findByEmail(email);
        if (existing.isPresent()) {
            throw new IllegalArgumentException(ErrorMessages.ALREADY_REGISTERED_EMAIL);
        }
        String encodedPassword = passwordEncoder.encode(password);
        Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
                .address(address)
                .authority("ADMIN")
                .verified(true)
                .build();
        return memberRepository.save(member);
    }

    // 일반 회원 로그인 (이메일 인증 여부 포함)
    public Member login(String email, String rawPassword) {
        log.info("일반 회원 로그인 시도: {}", email);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.MEMBER_NOT_FOUND));
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new IllegalArgumentException(ErrorMessages.PASSWORD_MISMATCH);
        }
        if (!member.isVerified()) {
            throw new IllegalArgumentException(ErrorMessages.EMAIL_NOT_VERIFIED);
        }
        return member;
    }

    // 관리자 로그인: 추가로 권한 확인
    public Member loginAdmin(String email, String rawPassword) {
        log.info("관리자 로그인 시도: {}", email);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.MEMBER_NOT_FOUND));
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new IllegalArgumentException(ErrorMessages.PASSWORD_MISMATCH);
        }
        if (!"ADMIN".equalsIgnoreCase(member.getAuthority())) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_ADMIN_CODE);
        }
        return member;
    }

    // 리프레시 토큰 재발급 시 회원 조회에 사용
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.MEMBER_NOT_FOUND));
    }

    public boolean verifyEmail(String email, String code) {
        return emailVerificationService.verifyEmail(email, code);
    }
}
