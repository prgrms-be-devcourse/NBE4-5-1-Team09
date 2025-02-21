package com.example.cafe.domain.member.service;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
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

    @Test
    @DisplayName("일반 회원 가입 - 정상 동작 테스트")
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
    @DisplayName("일반 회원 가입 - 중복 이메일 실패 테스트")
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
    @DisplayName("일반 회원 로그인 - 정상 동작 테스트")
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
        // 수정: any(Member.class) 사용하여 인자 매칭
        when(authTokenService.genAccessToken(any(Member.class))).thenReturn(token);

        Member resultMember = memberService.login(email, password);
        String resultToken = authTokenService.genAccessToken(resultMember);

        assertEquals(token, resultToken);
    }

    @Test
    @DisplayName("일반 회원 로그인 - 회원 정보 없음 실패 테스트")
    public void t4() {
        String email = "test@test.com";
        String password = "testtest";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            memberService.login(email, password);
        });
        assertEquals(ErrorMessages.MEMBER_NOT_FOUND, ex.getMessage());
    }

    @Test
    @DisplayName("일반 회원 로그인 - 비밀번호 불일치 실패 테스트")
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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            memberService.login(email, password);
        });
        assertEquals(ErrorMessages.PASSWORD_MISMATCH, ex.getMessage());
    }

    @Test
    @DisplayName("일반 회원 로그인 - 이메일 미인증 실패 테스트")
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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            memberService.login(email, password);
        });
        assertEquals(ErrorMessages.EMAIL_NOT_VERIFIED, ex.getMessage());
    }

    @Test
    @DisplayName("관리자 회원 가입 - 정상 동작 테스트")
    public void t7() {
        ReflectionTestUtils.setField(memberService, "secretAdminCode", "adminValue");

        String email = "admin@test.com";
        String password = "testtest";
        String address = "test";
        String providedAdminCode = "adminValue"; // 실제 프로퍼티 값 사용

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
    @DisplayName("관리자 회원 가입 - 중복 이메일 실패 테스트")
    public void t8() {
        ReflectionTestUtils.setField(memberService, "secretAdminCode", "adminValue");

        String email = "admin@test.com";
        String password = "testtest";
        String address = "test";
        String providedAdminCode = "adminValue";

        // 이미 등록된 이메일 존재
        Member existing = Member.builder().email(email).build();
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            memberService.joinAdmin(email, password, address, providedAdminCode);
        });
        assertEquals(ErrorMessages.ALREADY_REGISTERED_EMAIL, ex.getMessage());
    }

    @Test
    @DisplayName("관리자 회원 가입 - 잘못된 admin code 실패 테스트")
    public void t9() {
        ReflectionTestUtils.setField(memberService, "secretAdminCode", "adminValue");

        String email = "admin@test.com";
        String password = "testtest";
        String address = "test";
        // 잘못된 admin code 제공
        String providedAdminCode = "wrongCode";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            memberService.joinAdmin(email, password, address, providedAdminCode);
        });
        assertEquals(ErrorMessages.INVALID_ADMIN_CODE, ex.getMessage());
    }

    @Test
    @DisplayName("관리자 회원 가입 - 비밀번호 암호화 호출 확인 테스트")
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
        // 비밀번호 암호화가 한 번 호출되었는지 검증
        verify(passwordEncoder, times(1)).encode(password);
        assertEquals(encodedPassword, result.getPassword());
    }

    @Test
    @DisplayName("관리자 회원 가입 - Repository 저장 호출 테스트")
    public void t11() {
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

        memberService.joinAdmin(email, password, address, providedAdminCode);
        // Repository의 save 메서드가 한 번 호출되었는지 검증
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("관리자 회원 가입 - 생성된 회원 필드 값 검증 테스트")
    public void t12() {
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
        assertEquals(email, result.getEmail());
        assertEquals(address, result.getAddress());
        assertEquals("ADMIN", result.getAuthority());
        assertTrue(result.isVerified());
    }

    @Test
    @DisplayName("관리자 회원 가입 - 비밀번호 암호화 예외 상황 테스트")
    public void t13() {
        ReflectionTestUtils.setField(memberService, "secretAdminCode", "adminValue");

        String email = "admin@test.com";
        String password = "testtest";
        String address = "test";
        String providedAdminCode = "adminValue";

        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
        // 암호화 중 예외 발생 시뮬레이션
        when(passwordEncoder.encode(password)).thenThrow(new RuntimeException("Encoding error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            memberService.joinAdmin(email, password, address, providedAdminCode);
        });
        assertEquals("Encoding error", ex.getMessage());
    }

    @Test
    @DisplayName("회원 탈퇴 - 정상 동작 테스트")
    public void t14() {
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

        // deleteMember 내부에서는 login()을 재사용하므로 repository.delete()가 호출되어야 함
        doNothing().when(memberRepository).delete(member);

        // 회원 탈퇴 호출
        memberService.deleteMember(email, password);

        // repository.delete()가 정확히 한 번 호출되었는지 검증
        verify(memberRepository, times(1)).delete(member);
    }

    @Test
    @DisplayName("회원 탈퇴 - 존재하지 않는 회원 실패 테스트")
    public void t15() {
        String email = "test@test.com";
        String password = "testtest";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            memberService.deleteMember(email, password);
        });
        assertEquals(ErrorMessages.MEMBER_NOT_FOUND, ex.getMessage());
    }

    @Test
    @DisplayName("회원 탈퇴 - 비밀번호 불일치 실패 테스트")
    public void t16() {
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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            memberService.deleteMember(email, password);
        });
        assertEquals(ErrorMessages.PASSWORD_MISMATCH, ex.getMessage());
    }

}
