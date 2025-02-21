package com.example.cafe.integration;

import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.repository.MemberRepository;
import com.example.cafe.domain.member.service.MemberService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class MemberIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        // 테스트 시작 전 모든 회원 데이터를 삭제합니다.
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 및 관리자 회원가입 테스트")
    public void testSignUpAndAdminSignUp() throws Exception {
        // 회원가입
        String joinRequest = "{ \"email\": \"user@test.com\", \"password\": \"userpass\", \"address\": \"User Address\" }";
        mvc.perform(post("/api/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinRequest))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("회원 가입 성공: user@test.com")));

        // 관리자 회원가입
        ReflectionTestUtils.setField(memberService, "secretAdminCode", "secret");

        String adminJoinRequest = "{ \"email\": \"admin@test.com\", \"password\": \"adminpass\", \"address\": \"Admin Address\", \"adminCode\": \"secret\" }";
        mvc.perform(post("/api/member/join/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(adminJoinRequest))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("관리자 회원 가입 성공: admin@test.com")));
    }

    @Test
    @DisplayName("일반 회원 로그인 및 액세스 토큰 반환 테스트")
    public void testLogin() throws Exception {
        // 테스트용 회원 생성 (이메일 인증 완료 상태)
        String email = "user@test.com";
        String rawPassword = "userpass";
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .address("User Address")
                .authority("USER")
                .verified(true)
                .build();
        memberRepository.save(member);

        String loginRequest = "{ \"email\": \"" + email + "\", \"password\": \"" + rawPassword + "\" }";
        MvcResult loginResult = mvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andReturn();

        // 액세스 토큰은 JSON 응답에 포함되고, 리프레시 토큰은 쿠키에 설정됨
        String loginResponse = loginResult.getResponse().getContentAsString();
        Map<String, String> loginResponseMap = objectMapper.readValue(loginResponse, new TypeReference<Map<String, String>>() {});
        String accessToken = loginResponseMap.get("token");
        assertNotNull(accessToken);

        // 리프레시 토큰 쿠키 확인
        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
        assertNotNull(refreshCookie);
    }

    @Test
    @DisplayName("관리자 로그인 테스트")
    public void testAdminLogin() throws Exception {
        // 테스트용 관리자 회원 생성
        String email = "admin@test.com";
        String rawPassword = "adminpass";
        Member adminMember = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .address("Admin Address")
                .authority("ADMIN")
                .verified(true)
                .build();
        memberRepository.save(adminMember);

        String loginRequest = "{ \"email\": \"" + email + "\", \"password\": \"" + rawPassword + "\" }";
        mvc.perform(post("/api/member/login/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @DisplayName("액세스 토큰 재발급 (리프레시 토큰 사용) 테스트")
    public void testRefreshToken() throws Exception {
        // 테스트용 회원 생성
        String email = "user@test.com";
        String rawPassword = "userpass";
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .address("User Address")
                .authority("USER")
                .verified(true)
                .build();
        memberRepository.save(member);

        // 먼저 로그인하여 리프레시 토큰 쿠키 획득
        String loginRequest = "{ \"email\": \"" + email + "\", \"password\": \"" + rawPassword + "\" }";
        MvcResult loginResult = mvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();
        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
        assertNotNull(refreshCookie);

        // /refresh 엔드포인트 호출
        MvcResult refreshResult = mvc.perform(post("/api/member/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String refreshResponse = refreshResult.getResponse().getContentAsString();
        Map<String, String> refreshResponseMap = objectMapper.readValue(refreshResponse, new TypeReference<Map<String, String>>() {});
        String newAccessToken = refreshResponseMap.get("token");
        assertNotNull(newAccessToken);
    }

    @Test
    @DisplayName("이메일 인증 통합 테스트")
    public void testVerifyEmailUsingDBCode() throws Exception {
        // 회원가입 요청
        String email = "verify@test.com";
        String joinRequest = "{ \"email\": \"" + email + "\", \"password\": \"pass123\", \"address\": \"Some Address\" }";
        mvc.perform(post("/api/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinRequest))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("회원 가입 성공: " + email)));

        // 가입 후, DB에서 해당 회원을 조회하여 인증 코드를 가져옵니다.
        // (테스트 환경에서는 EmailVerificationService가 실제 이메일 전송 대신 회원 객체의 verificationCode를 설정했어야 함)
        Member member = memberRepository.findByEmail(email).orElseThrow();
        String verificationCode = member.getVerificationCode();
        assertNotNull(verificationCode, "회원가입 시 생성된 인증 코드는 null이면 안 됩니다.");

        // 이메일 인증 요청
        String verifyRequest = "{ \"email\": \"" + email + "\", \"code\": \"" + verificationCode + "\" }";
        mvc.perform(post("/api/member/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyRequest))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("이메일 인증 성공")));
    }

    @Test
    @DisplayName("로그아웃 통합 테스트")
    public void testLogoutIntegration() throws Exception {
        // 1. 테스트용 회원 생성 및 로그인 요청
        String email = "logout@test.com";
        String rawPassword = "logoutpass";
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .address("Logout Address")
                .authority("USER")
                .verified(true)
                .build();
        memberRepository.save(member);

        String loginRequest = "{ \"email\":\"" + email + "\", \"password\":\"" + rawPassword + "\" }";
        MvcResult loginResult = mvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        // 로그인 응답에서 refreshToken 쿠키 확인
        Cookie refreshCookie = loginResult.getResponse().getCookie("refreshToken");
        assertNotNull(refreshCookie, "리프레시 토큰 쿠키가 있어야 합니다.");

        // 2. 로그아웃 요청
        mvc.perform(post("/api/member/logout")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("로그아웃 성공")))
                .andExpect(result -> {
                    String setCookie = result.getResponse().getHeader("Set-Cookie");
                    assertNotNull(setCookie);
                    assertTrue(setCookie.contains("refreshToken="));
                    assertTrue(setCookie.contains("Max-Age=0"));
                });
    }

    @Test
    @DisplayName("회원 탈퇴 통합 테스트 (로그인 상태)")
    public void testMemberDeletionIntegration() throws Exception {
        // 1. 테스트용 회원 생성
        String email = "delete@test.com";
        String rawPassword = "deletepass";
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .address("Delete Address")
                .authority("USER")
                .verified(true)
                .build();
        memberRepository.save(member);

        // 2. 로그인 요청하여 액세스 토큰 획득
        String loginRequest = "{ \"email\":\"" + email + "\", \"password\":\"" + rawPassword + "\" }";
        MvcResult loginResult = mvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();
        String loginResponse = loginResult.getResponse().getContentAsString();
        Map<String, String> loginResponseMap = objectMapper.readValue(loginResponse, new TypeReference<Map<String, String>>() {});
        String accessToken = loginResponseMap.get("token");
        assertNotNull(accessToken);

        // 3. 회원 탈퇴 요청 (Authorization 헤더에 액세스 토큰 포함)
        String deleteRequest = "{ \"email\":\"" + email + "\", \"password\":\"" + rawPassword + "\" }";
        mvc.perform(delete("/api/member/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(deleteRequest))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("회원 탈퇴 성공")));

        // 4. 탈퇴 후, 해당 회원이 DB에 존재하지 않는지 확인
        Optional<Member> deletedMember = memberRepository.findByEmail(email);
        assertFalse(deletedMember.isPresent(), "회원을 정상적으로 삭제해야 합니다.");
    }
}
