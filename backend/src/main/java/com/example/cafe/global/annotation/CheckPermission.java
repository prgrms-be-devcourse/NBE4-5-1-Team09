package com.example.cafe.global.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) // 런타임에도 유지
@Target(ElementType.METHOD)       // 메서드에 적용
public @interface CheckPermission {
    String value(); // 예: "ADMIN", "USER"
}