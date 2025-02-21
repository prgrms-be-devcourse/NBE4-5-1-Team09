package com.example.cafe.domain.member.controller;

import com.example.cafe.domain.member.dto.*;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.service.AuthTokenService;
import com.example.cafe.domain.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MemberController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class
        }
)
@ActiveProfiles("test")
@ContextConfiguration(classes = {MemberController.class, MemberControllerTest.TestConfig.class})
@AutoConfigureMockMvc(addFilters = false)
public class MemberControllerTest {

    @TestConfiguration
    static class TestConfig {
    }

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MemberService memberService;

    @MockBean
    private AuthTokenService authTokenService;

    @Autowired
    private ObjectMapper objectMapper;


    private final String sampleAccessToken = "sampleAccessToken";
    private final String testEmail = "test@test.com";

    private Map<String, Object> createClaims(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        return claims;
    }

    @Test
    @DisplayName("회원 가입 정상 동작 확인")
    public void t1() throws Exception {
        MemberJoinRequestDto request = new MemberJoinRequestDto();
        request.setEmail("test@test.com");
        request.setPassword("testtest");
        request.setAddress("test test test");

        Member member = Member.builder().email("test@test.com").build();
        when(memberService.join(anyString(), anyString(), anyString())).thenReturn(member);

        mvc.perform(post("/api/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("회원 가입 성공: test@test.com")));
    }

    @Test
    @DisplayName("회원 가입 필수 필드 누락")
    public void t2() throws Exception {
        MemberJoinRequestDto request = new MemberJoinRequestDto();
        request.setEmail("");
        request.setPassword("");
        request.setAddress("");

        mvc.perform(post("/api/member/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("일반 유저 로그인 정상 동작 확인")
    public void t3() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@test.com");
        request.setPassword("testtest");

        // MemberService.login은 Member 객체를 반환하도록 목킹
        Member member = Member.builder().email("test@test.com").build();
        when(memberService.login(anyString(), anyString())).thenReturn(member);

        // AuthTokenService에서 토큰 생성 목킹
        when(authTokenService.genAccessToken(member)).thenReturn("dummytoken");
        when(authTokenService.genRefreshToken(member)).thenReturn("dummyrefresh");

        mvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("dummytoken"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    @DisplayName("관리자 회원 가입 정상 동작 확인")
    public void t4() throws Exception {
        AdminJoinRequestDto request = new AdminJoinRequestDto();
        request.setEmail("admin@test.com");
        request.setPassword("admintest");
        request.setAddress("Seoul");
        request.setAdminCode("secret");

        Member adminMember = Member.builder().email("admin@test.com").build();
        when(memberService.joinAdmin(anyString(), anyString(), anyString(), anyString())).thenReturn(adminMember);

        mvc.perform(post("/api/member/join/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("관리자 회원 가입 성공: admin@test.com")));
    }

    @Test
    @DisplayName("관리자 회원 가입 필수 필드 검증 실패")
    public void t5() throws Exception {
        AdminJoinRequestDto request = new AdminJoinRequestDto();
        request.setEmail("");
        request.setPassword("");
        request.setAddress("");
        request.setAdminCode("");

        mvc.perform(post("/api/member/join/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("관리자 로그인 정상 동작 확인")
    public void t6() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("admin@test.com");
        request.setPassword("admintest");

        // 관리자 로그인은 MemberService.loginAdmin이 Member 객체를 반환하도록 목킹
        Member adminMember = Member.builder().email("admin@test.com").build();
        when(memberService.loginAdmin(anyString(), anyString())).thenReturn(adminMember);
        when(authTokenService.genAccessToken(adminMember)).thenReturn("adminToken");
        when(authTokenService.genRefreshToken(adminMember)).thenReturn("adminRefresh");

        mvc.perform(post("/api/member/login/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("adminToken"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    @DisplayName("이메일 인증 성공 시 정상 응답 확인")
    public void t7() throws Exception {
        EmailVerificationRequestDto request = new EmailVerificationRequestDto();
        request.setEmail("test@example.com");
        request.setCode("12345678");

        when(memberService.verifyEmail(anyString(), anyString())).thenReturn(true);

        mvc.perform(post("/api/member/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("이메일 인증 성공"));
    }

    @Test
    @DisplayName("이메일 인증 실패 시 정상 응답 확인")
    public void t8() throws Exception {
        EmailVerificationRequestDto request = new EmailVerificationRequestDto();
        request.setEmail("test@example.com");
        request.setCode("wrongCode");

        when(memberService.verifyEmail(anyString(), anyString())).thenReturn(false);

        mvc.perform(post("/api/member/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("이메일 인증 실패"));
    }

    @Test
    @DisplayName("액세스 토큰 재발급 정상 동작 확인")
    public void t9() throws Exception {
        String refreshToken = "validRefreshToken";
        // verifyToken()이 유효한 토큰으로 인식되어 클레임 맵을 반환하도록 설정
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@test.com");
        when(authTokenService.verifyToken(refreshToken)).thenReturn(claims);

        // 클레임의 이메일로 회원 조회 후, 새로운 액세스 토큰 생성
        Member member = Member.builder().email("test@test.com").build();
        when(memberService.findByEmail("test@test.com")).thenReturn(member);
        when(authTokenService.genAccessToken(member)).thenReturn("newDummyToken");

        mvc.perform(post("/api/member/refresh")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("newDummyToken"));
    }

    @Test
    @DisplayName("로그아웃 정상 동작 확인")
    public void t10() throws Exception {
        mvc.perform(post("/api/member/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("로그아웃 성공")))
                .andExpect(result -> {
                    // 응답 헤더의 Set-Cookie에 refreshToken 쿠키가 삭제된 형태가 포함되어 있는지 확인
                    String setCookie = result.getResponse().getHeader("Set-Cookie");
                    assertNotNull(setCookie);
                    assertTrue(setCookie.contains("refreshToken="));
                    assertTrue(setCookie.contains("Max-Age=0"));
                });
    }

    @Test
    @DisplayName("회원 탈퇴 정상 동작 확인 (로그인 상태)")
    public void t11() throws Exception {
        // 탈퇴 요청에 사용할 이메일과 비밀번호
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@test.com");
        request.setPassword("testPassword");

        // 액세스 토큰 검증을 위한 클레임 설정 (토큰에 담긴 이메일)
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@test.com");
        // "validAccessToken"이라는 액세스 토큰이 유효하다고 가정하고 클레임 반환하도록 목킹
        when(authTokenService.verifyToken("validAccessToken")).thenReturn(claims);

        // memberService.deleteMember(email, password)가 호출될 때 아무런 예외 없이 진행되도록 목킹
        doNothing().when(memberService).deleteMember(anyString(), anyString());

        mvc.perform(delete("/api/member/delete")
                        .header("Authorization", "Bearer validAccessToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("회원 탈퇴 성공")));
    }

    @Test
    @DisplayName("프로필 조회 테스트")
    public void t12() throws Exception {
        ProfileResponseDto profile = new ProfileResponseDto();
        profile.setEmail(testEmail);
        profile.setAddress("Old Address");
        profile.setAuthority("USER");

        when(authTokenService.verifyToken(anyString())).thenReturn(createClaims(testEmail));
        when(memberService.getProfile(testEmail)).thenReturn(profile);

        mvc.perform(MockMvcRequestBuilders.get("/api/member/profile")
                        .header("Authorization", "Bearer " + sampleAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.address").value("Old Address"));
    }

    @Test
    @DisplayName("프로필 수정 테스트")
    public void t13() throws Exception {
        String newAddress = "New Address";
        ProfileUpdateRequestDto updateDto = new ProfileUpdateRequestDto();
        updateDto.setAddress(newAddress);

        ProfileResponseDto updatedProfile = new ProfileResponseDto();
        updatedProfile.setEmail(testEmail);
        updatedProfile.setAddress(newAddress);
        updatedProfile.setAuthority("USER");

        when(authTokenService.verifyToken(anyString())).thenReturn(createClaims(testEmail));
        when(memberService.updateProfile(eq(testEmail), any(ProfileUpdateRequestDto.class)))
                .thenReturn(updatedProfile);

        mvc.perform(MockMvcRequestBuilders.put("/api/member/profile")
                        .header("Authorization", "Bearer " + sampleAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value(newAddress));
    }

    @Test
    @DisplayName("비밀번호 변경 테스트")
    public void t14() throws Exception {
        ChangePasswordRequestDto changeDto = new ChangePasswordRequestDto();
        changeDto.setOldPassword("oldPass");
        changeDto.setNewPassword("newPass");

        when(authTokenService.verifyToken(anyString())).thenReturn(createClaims(testEmail));
        // 서비스 메서드가 정상 동작한다고 가정하므로 예외가 발생하지 않음

        mvc.perform(MockMvcRequestBuilders.post("/api/member/change-password")
                        .header("Authorization", "Bearer " + sampleAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("비밀번호 변경 성공")));
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 테스트")
    public void t15() throws Exception {
        PasswordResetRequestDto resetReq = new PasswordResetRequestDto();
        resetReq.setEmail(testEmail);

        // 서비스 호출 시 예외가 발생하지 않는다고 가정
        mvc.perform(MockMvcRequestBuilders.post("/api/member/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetReq)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("비밀번호 재설정 이메일을 전송했습니다.")));
    }

    @Test
    @DisplayName("비밀번호 재설정 확인 테스트")
    public void t16() throws Exception {
        PasswordResetConfirmRequestDto confirmDto = new PasswordResetConfirmRequestDto();
        confirmDto.setEmail(testEmail);
        confirmDto.setResetCode("123456");
        confirmDto.setNewPassword("newPassword");

        // 서비스 메서드가 정상적으로 작동하여 예외가 발생하지 않는다고 가정
        mvc.perform(MockMvcRequestBuilders.post("/api/member/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("비밀번호 재설정 성공")));
    }
}
