package com.example.cafe.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화 (개발 단계에서는 H2 콘솔 접근 용이)
                .csrf(csrf -> csrf.disable())
                // H2 콘솔 접근을 허용하기 위해 frameOptions 비활성화
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                // 세션을 사용하지 않는 stateless 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // H2 콘솔 및 기타 엔드포인트 접근 설정
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
