package com.example.cafe.global.aop;

import com.example.cafe.global.annotation.CheckPermission;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class CheckPermissionAspect {

    @Pointcut("@annotation(com.example.cafe.global.annotation.CheckPermission)")
    public void checkPermissionPointcut() {}

    @Before("checkPermissionPointcut()")
    public void before(JoinPoint joinPoint) {
        // 현재 인증된 사용자 정보 획득
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }

        // 메서드에 붙은 어노테이션 정보 획득
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckPermission checkPermission = method.getAnnotation(CheckPermission.class);

        if (checkPermission != null) {
            String requiredRole = checkPermission.value(); // 예: "ADMIN"
            // 현재 사용자 권한 중에 requiredRole이 있는지 확인
            boolean hasRole = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(requiredRole));

            if (!hasRole) {
                throw new AccessDeniedException("접근 권한이 없습니다: " + requiredRole);
            }
        }
    }
}
