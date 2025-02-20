package com.example.cafe.domain.member.controller;

import com.example.cafe.domain.member.dto.AdminJoinRequestDto;
import com.example.cafe.domain.member.dto.EmailVerificationRequestDto;
import com.example.cafe.domain.member.dto.LoginRequestDto;
import com.example.cafe.domain.member.dto.MemberJoinRequestDto;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MemberController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class
        }
)
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

}
