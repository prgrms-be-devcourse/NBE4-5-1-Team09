//package com.example.cafe.global.util;
//
//import com.example.cafe.domain.member.service.AuthTokenService;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.OncePerRequestFilter;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final AuthTokenService authTokenService;
//    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
//
//    public JwtAuthenticationFilter(AuthTokenService authTokenService) {
//        this.authTokenService = authTokenService;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//        String header = request.getHeader("Authorization");
//        if (header != null && header.startsWith("Bearer ")) {
//            String token = header.substring(7);
//            logger.debug("JWT 토큰 추출: {}", token);
//            Map<String, Object> claims = authTokenService.verifyToken(token);
//            if (claims != null) {
//                logger.debug("JWT 클레임: {}", claims);
//                String email = (String) claims.get("email");
//                String authority = (String) claims.get("authority");
//                logger.debug("추출된 이메일: {}, 권한: {}", email, authority);
//                UsernamePasswordAuthenticationToken authentication =
//                        new UsernamePasswordAuthenticationToken(
//                                email,
//                                null,
//                                List.of(new SimpleGrantedAuthority(authority))
//                        );
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//                logger.debug("SecurityContext에 인증 정보 설정 완료: {}", SecurityContextHolder.getContext().getAuthentication());
//            } else {
//                logger.warn("유효하지 않은 JWT 토큰: {}", token);
//            }
//        } else {
//            logger.debug("Authorization 헤더가 없거나 'Bearer '로 시작하지 않음");
//        }
//        filterChain.doFilter(request, response);
//    }
//}
