package com.example.cafe.domain.member.controller;

import com.example.cafe.domain.member.dto.AdminJoinRequestDto;
import com.example.cafe.domain.member.dto.LoginRequestDto;
import com.example.cafe.domain.member.dto.MemberJoinRequestDto;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

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
// 명시적으로 컨트롤러와 테스트 전용 설정만 로드하도록 함
@ContextConfiguration(classes = {MemberController.class, MemberControllerTest.TestConfig.class})
@AutoConfigureMockMvc(addFilters = false)
public class MemberControllerTest {

    @TestConfiguration
    static class TestConfig {
    }

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원 가입 정상 동작 확인")
    public void t1() throws Exception {
        MemberJoinRequestDto request = new MemberJoinRequestDto();
        request.setEmail("test@test.com");
        request.setPassword("testtest");
        request.setAddress("test test test");

        Member member = Member.builder()
                .email("test@test.com")
                .build();

        when(memberService.join(anyString(), anyString(), anyString()))
                .thenReturn(member);

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

        Mockito.when(memberService.login(anyString(), anyString())).thenReturn("dummytoken");

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
        Mockito.when(memberService.joinAdmin(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(adminMember);

        mvc.perform(post("/api/member/join/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("관리자 회원 가입 성공: admin@test.com")));
    }
}
