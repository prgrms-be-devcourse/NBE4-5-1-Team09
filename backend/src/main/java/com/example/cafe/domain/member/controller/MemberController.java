package com.example.cafe.domain.member.controller;

import com.example.cafe.domain.member.dto.*;
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

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMember(
            @RequestBody @Validated LoginRequestDto request,
            @RequestHeader("Authorization") String authHeader) {

        // Authorization 헤더가 없거나 Bearer 토큰 형식이 아닌 경우
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증 토큰이 제공되지 않았습니다.");
        }

        String accessToken = authHeader.substring("Bearer ".length());
        // 액세스 토큰 검증 및 클레임 추출
        Map<String, Object> claims = authTokenService.verifyToken(accessToken);
        if (claims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 액세스 토큰입니다.");
        }

        String tokenEmail = (String) claims.get("email");
        // 토큰에 포함된 이메일과 요청에 담긴 이메일이 일치하는지 확인
        if (!tokenEmail.equals(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("토큰 정보와 요청 이메일이 일치하지 않습니다.");
        }

        // 본인 인증 후 회원 탈퇴 진행
        memberService.deleteMember(request.getEmail(), request.getPassword());
        return ResponseEntity.ok("회원 탈퇴 성공");
    }

    // 프로필 조회: 액세스 토큰에서 이메일 추출하여 본인 정보 조회
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        String email = extractEmailFromToken(authHeader);
        ProfileResponseDto profile = memberService.getProfile(email);
        return ResponseEntity.ok(profile);
    }

    // 프로필 수정: 액세스 토큰에서 이메일 추출하여 본인 정보 수정
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody @Validated ProfileUpdateRequestDto dto) {
        String email = extractEmailFromToken(authHeader);
        ProfileResponseDto profile = memberService.updateProfile(email, dto);
        return ResponseEntity.ok(profile);
    }

    // 비밀번호 재설정 요청: 비밀번호 재설정을 위해 이메일 전송
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Validated PasswordResetRequestDto request) {
        memberService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok("비밀번호 재설정 이메일을 전송했습니다.");
    }

    // 비밀번호 재설정 확인: 이메일, 재설정 코드, 새 비밀번호를 받아 비밀번호를 변경
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Validated PasswordResetConfirmRequestDto request) {
        memberService.resetPassword(request.getEmail(), request.getResetCode(), request.getNewPassword());
        return ResponseEntity.ok("비밀번호 재설정 성공");
    }

    private String extractEmailFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("인증 토큰이 제공되지 않았습니다.");
        }
        String token = authHeader.substring("Bearer ".length());
        Map<String, Object> claims = authTokenService.verifyToken(token);
        if (claims == null || claims.get("email") == null) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
        return (String) claims.get("email");
    }
}
