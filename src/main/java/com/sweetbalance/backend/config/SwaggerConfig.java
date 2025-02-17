package com.sweetbalance.backend.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .paths(new Paths()
                        // 카카오 소셜 로그인 API
                        .addPathItem("/oauth2/authorization/kakao", new PathItem()
                                .get(new Operation()
                                        .addTagsItem("Auth")
                                        .summary("카카오 소셜 로그인")))

                        // 로그인 API
                        .addPathItem("/api/auth/sign-in", new PathItem()
                                .post(new Operation()
                                        .addTagsItem("Auth")
                                        .summary("일반 로그인")))

                        // 로그아웃 API
                        .addPathItem("/api/auth/sign-out", new PathItem()
                                .post(new Operation()
                                        .addTagsItem("Auth")
                                        .summary("로그아웃")))
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Sweet Balance API")
                .description("Sweet Balance 백엔드 API 문서")
                .version("1.0.0");
    }
}
