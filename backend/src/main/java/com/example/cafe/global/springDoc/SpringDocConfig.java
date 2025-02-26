package com.example.cafe.global.springDoc;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "CODE-Brew API Server", version = "v1"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SpringDocConfig {

    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("CODE-Brew api")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public GroupedOpenApi item() {
        return GroupedOpenApi.builder()
                .group("item")
                .pathsToMatch("/items/**")
                .build();
    }

    @Bean
    public GroupedOpenApi member() {
        return GroupedOpenApi.builder()
                .group("member")
                .pathsToMatch("/member/**")
                .build();
    }

    @Bean
    public GroupedOpenApi reviews() {
        return GroupedOpenApi.builder()
                .group("review")
                .pathsToMatch("/reviews/**")
                .build();
    }

    @Bean
    public GroupedOpenApi tradeAdmin() {
        return GroupedOpenApi.builder()
                .group("admin-trade")
                .pathsToMatch("/admin/trade/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userCart() {
        return GroupedOpenApi.builder()
                .group("user-cart")
                .pathsToMatch("/cart/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userTrade() {
        return GroupedOpenApi.builder()
                .group("user-trade")
                .pathsToMatch("/order/**")
                .build();
    }

    @Bean
    public GroupedOpenApi webHook() {
        return GroupedOpenApi.builder()
                .group("portone webhook")
                .pathsToMatch("/portone/**")
                .build();
    }
}