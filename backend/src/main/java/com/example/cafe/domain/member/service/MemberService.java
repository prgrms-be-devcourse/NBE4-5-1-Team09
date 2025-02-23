package com.example.cafe.domain.member.service;

import com.example.cafe.domain.member.dto.MemberProfileDto;
import com.example.cafe.domain.member.dto.ProfileResponseDto;
import com.example.cafe.domain.member.dto.ProfileUpdateRequestDto;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.global.constant.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final EmailVerificationService emailVerificationService;
    private final MailService mailService;

    @Value("${custom.admin.admin-code}")
    private String secretAdminCode;

    // 일반 회원 가입
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

    // 일반 회원 로그인
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

    // 회원 탈퇴 기능
    @Transactional
    public void deleteMember(String email, String rawPassword) {
        Member member = login(email, rawPassword);
        memberRepository.delete(member);
        log.info("회원 탈퇴 성공: {}", email);
    }

    // 프로필 조회
    public ProfileResponseDto getProfile(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.MEMBER_NOT_FOUND));
        ProfileResponseDto dto = new ProfileResponseDto();
        dto.setEmail(member.getEmail());
        dto.setAddress(member.getAddress());
        dto.setAuthority(member.getAuthority());
        return dto;
    }

    // 프로필 수정
    @Transactional
    public ProfileResponseDto updateProfile(String email, ProfileUpdateRequestDto dto) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.MEMBER_NOT_FOUND));
        member.setAddress(dto.getAddress());
        member = memberRepository.save(member);
        return getProfile(email);
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.MEMBER_NOT_FOUND));
        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            throw new IllegalArgumentException(ErrorMessages.PASSWORD_MISMATCH);
        }
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }


    // 비밀번호 재설정 이메일 전송 기능
    @Transactional
    public void requestPasswordReset(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.MEMBER_NOT_FOUND));
        //재설정 코드 6자리 숫자 생성
        String resetCode = String.format("%06d", (int)(Math.random()*1000000));
        member.setResetPasswordCode(resetCode);
        memberRepository.save(member);
        try {
            mailService.sendPasswordResetEmail(member.getEmail(), resetCode);
            log.info("비밀번호 재설정 이메일 전송 성공: {}", member.getEmail());
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 전송 실패: {}", member.getEmail(), e);
            throw new RuntimeException("비밀번호 재설정 이메일 전송 실패: " + e.getMessage());
        }
    }

    // 비밀번호 재설정 기능
    @Transactional
    public void resetPassword(String email, String resetCode, String newPassword) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.MEMBER_NOT_FOUND));
        if (!resetCode.equals(member.getResetPasswordCode())) {
            throw new IllegalArgumentException(ErrorMessages.RESET_CODE_MISMATCH);
        }
        member.setPassword(passwordEncoder.encode(newPassword));
        member.setResetPasswordCode(null);
        memberRepository.save(member);
        log.info("비밀번호 재설정 성공: {}", email);
    }

    public List<MemberProfileDto> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        return members.stream()
                .map(member -> MemberProfileDto.builder()
                        .id(member.getId())
                        .email(member.getEmail())
                        .address(member.getAddress())
                        .authority(member.getAuthority())
                        .build())
                .collect(Collectors.toList());
    }
}
