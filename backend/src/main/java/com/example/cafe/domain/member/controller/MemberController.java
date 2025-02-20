package com.example.cafe.domain.member.controller;

import com.example.cafe.domain.member.dto.AdminJoinRequestDto;
import com.example.cafe.domain.member.dto.EmailVerificationRequestDto;
import com.example.cafe.domain.member.dto.LoginRequestDto;
import com.example.cafe.domain.member.dto.MemberJoinRequestDto;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 일반 회원 가입
    @PostMapping("/join")
    public ResponseEntity<?> signUp(@RequestBody @Validated MemberJoinRequestDto request) {
        Member member = memberService.join(request.getEmail(), request.getPassword(), request.getAddress());
        return ResponseEntity.ok("회원 가입 성공: " + member.getEmail());
    }

    // 관리자 회원 가입
    @PostMapping("/join/admin")
    public ResponseEntity<?> signUpAdmin(@RequestBody @Validated AdminJoinRequestDto request) {
        Member member = memberService.joinAdmin(request.getEmail(), request.getPassword(), request.getAddress(), request.getAdminCode());
        return ResponseEntity.ok("관리자 회원 가입 성공: " + member.getEmail());
    }

    // 일반 회원 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequestDto request) {
        String token = memberService.login(request.getEmail(), request.getPassword());
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", request.getEmail());
        return ResponseEntity.ok(response);
    }

    // 관리자 로그인
    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody @Validated LoginRequestDto request) {
        String token = memberService.loginAdmin(request.getEmail(), request.getPassword());
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", request.getEmail());
        return ResponseEntity.ok(response);
    }

    // 이메일 인증: 사용자가 이메일로 받은 인증 코드를 제출하여 이메일 인증을 완료
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody @Validated EmailVerificationRequestDto request) {
        boolean result = memberService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok(result ? "이메일 인증 성공" : "이메일 인증 실패");
    }
}
