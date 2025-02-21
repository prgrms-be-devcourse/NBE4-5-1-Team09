package com.example.cafe.domain.member.controller;

import com.example.cafe.domain.member.dto.AdminJoinRequestDto;
import com.example.cafe.domain.member.dto.EmailVerificationRequestDto;
import com.example.cafe.domain.member.dto.LoginRequestDto;
import com.example.cafe.domain.member.dto.MemberJoinRequestDto;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.service.AuthTokenService;
import com.example.cafe.domain.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final AuthTokenService authTokenService;

    // 일반 회원 가입 (이메일 인증 코드 발송 로직은 그대로 유지)
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

    // 일반 회원 로그인 (정상 로그인 메서드 호출 및 리프레시 토큰 HttpOnly 쿠키 설정)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequestDto request, HttpServletResponse response) {
        // 일반 회원 로그인은 login() 메서드를 호출 (이메일 인증 여부 등 기존 로직 그대로 사용)
        Member member = memberService.login(request.getEmail(), request.getPassword());
        String accessToken = authTokenService.genAccessToken(member);
        String refreshToken = authTokenService.genRefreshToken(member);

        // HttpOnly 쿠키에 리프레시 토큰 설정 (예: 7일 유효)
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(604800); // 7일 = 604800초
        response.addCookie(refreshCookie);

        Map<String, String> res = new HashMap<>();
        res.put("token", accessToken);
        res.put("email", request.getEmail());
        return ResponseEntity.ok(res);
    }

    // 관리자 로그인
    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody @Validated LoginRequestDto request, HttpServletResponse response) {
        Member member = memberService.loginAdmin(request.getEmail(), request.getPassword());
        String accessToken = authTokenService.genAccessToken(member);
        String refreshToken = authTokenService.genRefreshToken(member);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(604800); // 7일
        response.addCookie(refreshCookie);

        Map<String, String> res = new HashMap<>();
        res.put("token", accessToken);
        res.put("email", request.getEmail());
        return ResponseEntity.ok(res);
    }

    // 액세스 토큰 재발급 엔드포인트 (리프레시 토큰 쿠키 사용)
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰이 존재하지 않습니다.");
        }
        // 리프레시 토큰 검증 및 클레임 추출
        Map<String, Object> claims = authTokenService.verifyToken(refreshToken);
        if (claims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 리프레시 토큰입니다.");
        }
        String email = (String) claims.get("email");
        Member member = memberService.findByEmail(email);
        String newAccessToken = authTokenService.genAccessToken(member);
        Map<String, String> res = new HashMap<>();
        res.put("token", newAccessToken);
        return ResponseEntity.ok(res);
    }

    // 이메일 인증: 사용자가 이메일로 받은 인증 코드를 제출하여 이메일 인증을 완료
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody @Validated EmailVerificationRequestDto request) {
        boolean result = memberService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok(result ? "이메일 인증 성공" : "이메일 인증 실패");
    }

    @PostMapping("/logout")
    public ResponseEntity logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // 쿠키 삭제
        response.addCookie(cookie);
        return ResponseEntity.ok("로그아웃 성공");
    }
}
