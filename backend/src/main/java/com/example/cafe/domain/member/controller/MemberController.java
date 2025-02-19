package com.example.cafe.domain.member.controller;

import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    @Autowired
    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<?> signUp(@RequestParam String email,
                                    @RequestParam String password,
                                    @RequestParam String address) {
        Member member = memberService.join(email, password, address);
        return ResponseEntity.ok("회원 가입 성공: " + member.getEmail());
    }

    @PostMapping("/join/admin")
    public ResponseEntity<?> signUpAdmin(@RequestParam String email,
                                         @RequestParam String password,
                                         @RequestParam String address,
                                         @RequestParam String adminCode) {
        Member member = memberService.joinAdmin(email, password, address, adminCode);
        return ResponseEntity.ok("관리자 회원 가입 성공: " + member.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email,
                                   @RequestParam String password) {
        Member member = memberService.login(email, password);
        // 실제 토큰 발급 미구현
        return ResponseEntity.ok("로그인 성공: " + member.getEmail());
    }

    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestParam String email,
                                        @RequestParam String password) {
        Member member = memberService.loginAdmin(email, password);
        return ResponseEntity.ok("관리자 로그인 성공: " + member.getEmail());
    }
}

