package com.example.cafe.domain.member.controller;

import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
