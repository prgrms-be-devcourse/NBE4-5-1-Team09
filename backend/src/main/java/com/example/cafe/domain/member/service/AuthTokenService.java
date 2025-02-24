package com.example.cafe.domain.member.service;

import com.example.cafe.domain.member.entity.Member;
import com.example.cafe.global.util.Ut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthTokenService {

    @Value("${custom.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${custom.jwt.expire-seconds}")
    private int jwtExpireSeconds;

    @Value("${custom.jwt.refresh-expire-seconds}") // 예: 7일 (초 단위)
    private int jwtRefreshExpireSeconds;

    // 액세스 토큰 생성
    public String genAccessToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", member.getId());
        claims.put("email", member.getEmail());
        claims.put("authority", member.getAuthority());
        String token = Ut.Jwt.createToken(jwtSecretKey, jwtExpireSeconds, claims);
        log.info("JWT 액세스 토큰 생성: {}", member.getEmail());
        return token;
    }

    // 리프레시 토큰 생성
    public String genRefreshToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", member.getId());
        claims.put("email", member.getEmail());
        claims.put("authority", member.getAuthority());
        String token = Ut.Jwt.createToken(jwtSecretKey, jwtRefreshExpireSeconds, claims);
        log.info("JWT 리프레시 토큰 생성: {}", member.getEmail());
        return token;
    }

    // 토큰 검증 및 클레임 추출
    public Map<String, Object> verifyToken(String token) {
        if (!Ut.Jwt.isValidToken(jwtSecretKey, token)) {
            return null;
        }
        return Ut.Jwt.getPayload(jwtSecretKey, token);
    }

    public Long getIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("인증 토큰이 제공되지 않았습니다.");
        }
        String token = authHeader.substring("Bearer ".length());
        Map<String, Object> claims = verifyToken(token);
        if (claims == null || claims.get("id") == null) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
        Number id = (Number) claims.get("id");
        return id.longValue();
    }
}

