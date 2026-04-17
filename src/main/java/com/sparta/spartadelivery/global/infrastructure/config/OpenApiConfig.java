package com.sparta.spartadelivery.global.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String JWT_SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sparta Delivery API")
                        .description("Sparta Delivery 백엔드 API 문서")
                        .version("v1"))
                .components(new Components()
                        // Swagger UI의 Authorize 버튼에서 JWT access token을 입력할 수 있게 Bearer 인증 스키마를 등록한다.
                        .addSecuritySchemes(JWT_SECURITY_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                // 회원가입/로그인을 제외한 대부분의 API가 인증을 요구하므로 JWT 인증을 전역 기본값으로 둔다.
                .addSecurityItem(new SecurityRequirement().addList(JWT_SECURITY_SCHEME_NAME));
    }

    @Bean
    public GroupedOpenApi authOpenApi() {
        return GroupedOpenApi.builder()
                .group("api")
                .packagesToScan("com.sparta.spartadelivery")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
