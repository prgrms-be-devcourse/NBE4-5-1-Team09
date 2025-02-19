//package com.example.cafe.domain.member.service;
//
//import com.example.cafe.domain.member.entity.Member;
//import com.example.cafe.global.util.Ut;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class AuthTokenService {
//
//    @Value("${custom.jwt.secret-key}")
//    private String keyString;
//
//    @Value("${custom.jwt.expire-seconds}")
//    private int expireSeconds;
//
//    // 회원 정보를 기반으로 JWT 액세스 토큰 생성
//    public String genAccessToken(Member member) {
//        return Ut.Jwt.createToken(
//                keyString,
//                expireSeconds,
//                Map.of(
//                        "id", member.getId(),
//                        "email", member.getEmail(),
//                        "authority", member.getAuthority()
//                )
//        );
//    }
//
//    // 토큰의 payload 정보를 파싱하여 반환
//    public Map<String, Object> getPayload(String token) {
//        if (!Ut.Jwt.isValidToken(keyString, token)) {
//            return null;
//        }
//        Map<String, Object> payload = Ut.Jwt.getPayload(keyString, token);
//        Number idNo = (Number) payload.get("id");
//        long id = idNo.longValue();
//        String email = (String) payload.get("email");
//        String authority = (String) payload.get("authority");
//
//        return Map.of("id", id, "email", email, "authority", authority);
//    }
//}
