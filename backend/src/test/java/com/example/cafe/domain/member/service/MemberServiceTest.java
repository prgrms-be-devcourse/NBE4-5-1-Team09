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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("일반 회원 가입 정상 동작 테스트")
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
}
