package com.example.cafe.domain.member.controller;

import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> signUp(@RequestParam String email,
                                    @RequestParam String password,
                                    @RequestParam String address) {
        Member member = memberService.join(email, password, address);
        return ResponseEntity.ok("회원 가입 성공: " + member.getEmail());
    }

    // 관리자 회원 가입
    @PostMapping("/join/admin")
    public ResponseEntity<?> signUpAdmin(@RequestParam String email,
                                         @RequestParam String password,
                                         @RequestParam String address,
                                         @RequestParam String adminCode) {
        Member member = memberService.joinAdmin(email, password, address, adminCode);
        return ResponseEntity.ok("관리자 회원 가입 성공: " + member.getEmail());
    }

    // 일반 회원 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email,
                                   @RequestParam String password) {
        String token = memberService.login(email, password);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", email);
        return ResponseEntity.ok(response);
    }

    // 관리자 로그인
    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestParam String email,
                                        @RequestParam String password) {
        String token = memberService.loginAdmin(email, password);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", email);
        return ResponseEntity.ok(response);
    }

    // 이메일 인증: 사용자가 이메일로 받은 인증 코드를 제출하여 이메일 인증을 완료
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String email,
                                         @RequestParam String code) {
        boolean result = memberService.verifyEmail(email, code);
        return ResponseEntity.ok(result ? "이메일 인증 성공" : "이메일 인증 실패");
    }
}
