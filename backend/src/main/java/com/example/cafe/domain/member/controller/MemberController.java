package com.example.cafe.domain.member.controller;

import com.example.cafe.domain.member.dto.*;
import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.domain.member.service.AuthTokenService;
import com.example.cafe.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Parameter;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(name = "Member API", description = "회원 관련 API 엔드포인트")
public class MemberController {

    private final MemberService memberService;
    private final AuthTokenService authTokenService;

    @Operation(summary = "일반 회원 가입")
    @PostMapping("/join")
    public ResponseEntity<?> signUp(@RequestBody @Validated MemberJoinRequestDto request) {
        Member member = memberService.join(request.getEmail(), request.getPassword(), request.getAddress());
        return ResponseEntity.ok("회원 가입 성공: " + member.getEmail());
    }

    @Operation(summary = "관리자 회원 가입")
    @PostMapping("/join/admin")
    public ResponseEntity<?> signUpAdmin(@RequestBody @Validated AdminJoinRequestDto request) {
        Member member = memberService.joinAdmin(request.getEmail(), request.getPassword(), request.getAddress(), request.getAdminCode());
        return ResponseEntity.ok("관리자 회원 가입 성공: " + member.getEmail());
    }

    @Operation(summary = "일반 회원 로그인")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequestDto request, HttpServletResponse response) {
        Member member = memberService.login(request.getEmail(), request.getPassword());
        String accessToken = authTokenService.genAccessToken(member);
        String refreshToken = authTokenService.genRefreshToken(member);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(604800); // 7일
        response.addCookie(refreshCookie);

        Map<String, String> res = new HashMap<>();
        res.put("token", accessToken);
        res.put("email", request.getEmail());
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "관리자 로그인")
    @PostMapping("/login/admin")
    public ResponseEntity<?> loginAdmin(@RequestBody @Validated LoginRequestDto request, HttpServletResponse response) {
        Member member = memberService.loginAdmin(request.getEmail(), request.getPassword());
        String accessToken = authTokenService.genAccessToken(member);
        String refreshToken = authTokenService.genRefreshToken(member);

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(604800); // 7일
        response.addCookie(refreshCookie);

        Map<String, String> res = new HashMap<>();
        res.put("token", accessToken);
        res.put("email", request.getEmail());
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "액세스 토큰 재발급")
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

    @Operation(summary = "이메일 인증")
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody @Validated EmailVerificationRequestDto request) {
        boolean result = memberService.verifyEmail(request.getEmail(), request.getCode());
        return ResponseEntity.ok(result ? "이메일 인증 성공" : "이메일 인증 실패");
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 쿠키 삭제
        response.addCookie(cookie);
        return ResponseEntity.ok("로그아웃 성공");
    }

    @Operation(summary = "회원 탈퇴", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteMember(
            @RequestBody @Validated LoginRequestDto request,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증 토큰이 제공되지 않았습니다.");
        }

        String accessToken = authHeader.substring("Bearer ".length());
        Map<String, Object> claims = authTokenService.verifyToken(accessToken);
        if (claims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("유효하지 않은 액세스 토큰입니다.");
        }
        String tokenEmail = (String) claims.get("email");
        if (!tokenEmail.equals(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("토큰 정보와 요청 이메일이 일치하지 않습니다.");
        }

        memberService.deleteMember(request.getEmail(), request.getPassword());
        return ResponseEntity.ok("회원 탈퇴 성공");
    }

    @Operation(summary = "프로필 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String email = extractEmailFromToken(authHeader);
        ProfileResponseDto profile = memberService.getProfile(email);
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "프로필 수정", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Validated ProfileUpdateRequestDto dto) {
        String email = extractEmailFromToken(authHeader);
        ProfileResponseDto profile = memberService.updateProfile(email, dto);
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "비밀번호 변경", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Validated ChangePasswordRequestDto dto) {
        String email = extractEmailFromToken(authHeader);
        memberService.changePassword(email, dto.getOldPassword(), dto.getNewPassword());
        return ResponseEntity.ok("비밀번호 변경 성공");
    }

    @Operation(summary = "비밀번호 재설정 요청")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Validated PasswordResetRequestDto request) {
        memberService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok("비밀번호 재설정 이메일을 전송했습니다.");
    }

    @Operation(summary = "비밀번호 재설정 확인")
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
