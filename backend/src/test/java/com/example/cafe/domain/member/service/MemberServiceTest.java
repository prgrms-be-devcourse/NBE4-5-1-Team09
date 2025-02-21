package com.example.cafe.domain.member.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.cafe.domain.member.dto.ProfileResponseDto;
import com.example.cafe.domain.member.dto.ProfileUpdateRequestDto;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.global.constant.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private MailService mailService;

    @Test
    @DisplayName("t1: 일반 회원 가입 - 정상 동작 테스트")
    public void t1() throws Exception {
        String email = "test@test.com";
        String password = "testtest";
        String address = "test";
        String encodedPassword = "encodedTest";

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
                .address(address)
                .authority("USER")
                .verified(false)
                .build();
        when(memberRepository.save(any(Member.class))).thenReturn(member);
        doNothing().when(emailVerificationService).sendVerificationEmail(any(Member.class));

        Member result = memberService.join(email, password, address);
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(emailVerificationService, times(1)).sendVerificationEmail(any(Member.class));
    }

    @Test
    @DisplayName("t2: 일반 회원 가입 - 중복 이메일 실패 테스트")
    public void t2() {
        String email = "test@test.com";
        String password = "testtest";
        String address = "test";
        Member existing = Member.builder().email(email).build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            memberService.join(email, password, address);
        });
        assertEquals(ErrorMessages.ALREADY_REGISTERED_EMAIL, ex.getMessage());
    }

    @Test
    @DisplayName("t3: 일반 회원 로그인 - 정상 동작 테스트")
    public void t3() {
        String email = "test@test.com";
        String password = "testtest";
        String token = "dummyToken";
        Member member = Member.builder()
                .email(email)
                .password("encodedTest")
                .verified(true)
                .authority("USER")
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(password, member.getPassword())).thenReturn(true);
        when(authTokenService.genAccessToken(any(Member.class))).thenReturn(token);

        Member resultMember = memberService.login(email, password);
        String resultToken = authTokenService.genAccessToken(resultMember);

        assertEquals(token, resultToken);
    }

    @Test
    @DisplayName("t4: 일반 회원 로그인 - 회원 정보 없음 실패 테스트")
    public void t4() {
        String email = "test@test.com";
        String password = "testtest";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                memberService.login(email, password));
        assertEquals(ErrorMessages.MEMBER_NOT_FOUND, ex.getMessage());
    }

    @Test
    @DisplayName("t5: 일반 회원 로그인 - 비밀번호 불일치 실패 테스트")
    public void t5() {
        String email = "test@test.com";
        String password = "testtest";
        Member member = Member.builder()
                .email(email)
                .password("encodedTest")
                .verified(true)
                .authority("USER")
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(password, member.getPassword())).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                memberService.login(email, password));
        assertEquals(ErrorMessages.PASSWORD_MISMATCH, ex.getMessage());
    }

    @Test
    @DisplayName("t6: 일반 회원 로그인 - 이메일 미인증 실패 테스트")
    public void t6() {
        String email = "test@test.com";
        String password = "testtest";
        Member member = Member.builder()
                .email(email)
                .password("encodedTest")
                .verified(false)
                .authority("USER")
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(password, member.getPassword())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                memberService.login(email, password));
        assertEquals(ErrorMessages.EMAIL_NOT_VERIFIED, ex.getMessage());
    }

    @Test
    @DisplayName("t7: 관리자 회원 가입 - 정상 동작 테스트")
    public void t7() {
        ReflectionTestUtils.setField(memberService, "secretAdminCode", "adminValue");
        String email = "admin@test.com";
        String password = "testtest";
        String address = "test";
        String providedAdminCode = "adminValue";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
        String encodedPassword = providedAdminCode;
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        Member adminMember = Member.builder()
                .email(email)
                .password(encodedPassword)
                .address(address)
                .authority("ADMIN")
                .verified(true)
                .build();
        when(memberRepository.save(any(Member.class))).thenReturn(adminMember);

        Member result = memberService.joinAdmin(email, password, address, providedAdminCode);
        assertNotNull(result);
        assertEquals("ADMIN", result.getAuthority());
    }

    @Test
    @DisplayName("t8: 관리자 회원 가입 - 중복 이메일 실패 테스트")
    public void t8() {
        ReflectionTestUtils.setField(memberService, "secretAdminCode", "adminValue");
        String email = "admin@test.com";
        String password = "testtest";
        String address = "test";
        String providedAdminCode = "adminValue";
        Member existing = Member.builder().email(email).build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                memberService.joinAdmin(email, password, address, providedAdminCode));
        assertEquals(ErrorMessages.ALREADY_REGISTERED_EMAIL, ex.getMessage());
    }

    @Test
    @DisplayName("t9: 관리자 회원 가입 - 잘못된 admin code 실패 테스트")
    public void t9() {
        ReflectionTestUtils.setField(memberService, "secretAdminCode", "adminValue");
        String email = "admin@test.com";
        String password = "testtest";
        String address = "test";
        String providedAdminCode = "wrongCode";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                memberService.joinAdmin(email, password, address, providedAdminCode));
        assertEquals(ErrorMessages.INVALID_ADMIN_CODE, ex.getMessage());
    }

    @Test
    @DisplayName("t10: 관리자 회원 가입 - 비밀번호 암호화 호출 확인 테스트")
    public void t10() {
        ReflectionTestUtils.setField(memberService, "secretAdminCode", "adminValue");
        String email = "admin@test.com";
        String password = "testtest";
        String address = "test";
        String providedAdminCode = "adminValue";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
        String encodedPassword = "encodedAdminTest";
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        Member adminMember = Member.builder()
                .email(email)
                .password(encodedPassword)
                .address(address)
                .authority("ADMIN")
                .verified(true)
                .build();
        when(memberRepository.save(any(Member.class))).thenReturn(adminMember);

        Member result = memberService.joinAdmin(email, password, address, providedAdminCode);
        verify(passwordEncoder, times(1)).encode(password);
        assertEquals(encodedPassword, result.getPassword());
    }

    @Test
    @DisplayName("t11: 회원 탈퇴 - 정상 동작 테스트")
    public void t11() {
        String email = "test@test.com";
        String password = "testtest";
        String encodedPassword = "encodedPassword";
        Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
                .verified(true)
                .authority("USER")
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        doNothing().when(memberRepository).delete(member);

        memberService.deleteMember(email, password);
        verify(memberRepository, times(1)).delete(member);
    }

    @Test
    @DisplayName("t12: 회원 탈퇴 - 회원 정보 없음 실패 테스트")
    public void t12() {
        String email = "test@test.com";
        String password = "testtest";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                memberService.deleteMember(email, password));
        assertEquals(ErrorMessages.MEMBER_NOT_FOUND, ex.getMessage());
    }

    @Test
    @DisplayName("t13: 회원 탈퇴 - 비밀번호 불일치 실패 테스트")
    public void t13() {
        String email = "test@test.com";
        String password = "testtest";
        String encodedPassword = "encodedPassword";
        Member member = Member.builder()
                .email(email)
                .password(encodedPassword)
                .verified(true)
                .authority("USER")
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                memberService.deleteMember(email, password));
        assertEquals(ErrorMessages.PASSWORD_MISMATCH, ex.getMessage());
    }

    @Test
    @DisplayName("t14: 프로필 조회 테스트")
    public void t14() {
        String email = "test@test.com";
        Member member = Member.builder()
                .email(email)
                .address("Old Address")
                .authority("USER")
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        ProfileResponseDto profile = memberService.getProfile(email);
        assertEquals(email, profile.getEmail());
        assertEquals("Old Address", profile.getAddress());
        assertEquals("USER", profile.getAuthority());
    }

    @Test
    @DisplayName("t15: 프로필 수정 테스트")
    public void t15() {
        String email = "test@test.com";
        Member member = Member.builder()
                .email(email)
                .address("Old Address")
                .authority("USER")
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileUpdateRequestDto dto = new ProfileUpdateRequestDto();
        dto.setAddress("New Address");

        ProfileResponseDto updatedProfile = memberService.updateProfile(email, dto);
        assertEquals("New Address", updatedProfile.getAddress());
    }

    @Test
    @DisplayName("t16: 비밀번호 변경 성공 테스트")
    public void t16() {
        String email = "test@test.com";
        String oldPassword = "oldPass";
        String newPassword = "newPass";
        String encodedOld = "encodedOld";
        String encodedNew = "encodedNew";

        Member member = Member.builder()
                .email(email)
                .password(encodedOld)
                .verified(true)
                .build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(oldPassword, encodedOld)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNew);

        memberService.changePassword(email, oldPassword, newPassword);
        verify(memberRepository, times(1)).save(member);
        assertEquals(encodedNew, member.getPassword());
    }

    @Test
    @DisplayName("t17: 비밀번호 재설정 요청 성공 테스트")
    public void t17() throws Exception {
        String email = "test@test.com";
        Member member = Member.builder().email(email).build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(mailService).sendPasswordResetEmail(eq(email), anyString());

        memberService.requestPasswordReset(email);
        assertNotNull(member.getResetPasswordCode(), "재설정 코드가 저장되어 있어야 합니다.");
    }

    @Test
    @DisplayName("t18: 비밀번호 재설정 성공 테스트")
    public void t18() {
        String email = "test@test.com";
        String resetCode = "123456";
        String newPassword = "newPass";
        String encodedNew = "encodedNew";

        Member member = Member.builder().email(email).resetPasswordCode(resetCode).build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNew);

        memberService.resetPassword(email, resetCode, newPassword);
        assertEquals(encodedNew, member.getPassword());
        assertNull(member.getResetPasswordCode());
    }

    @Test
    @DisplayName("t19: 비밀번호 재설정 실패 - 잘못된 재설정 코드")
    public void t19() {
        String email = "test@test.com";
        String resetCode = "123456";
        String wrongCode = "654321";

        Member member = Member.builder().email(email).resetPasswordCode(resetCode).build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                memberService.resetPassword(email, wrongCode, "newPass"));
        assertEquals(ErrorMessages.RESET_CODE_MISMATCH, ex.getMessage());
    }
}
