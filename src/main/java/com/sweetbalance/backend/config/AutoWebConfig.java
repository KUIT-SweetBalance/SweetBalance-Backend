package com.sweetbalance.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan
public class AutoWebConfig implements WebMvcConfigurer {

    @Value("${spring.front.origin-netlify}")
    private String frontOriginNetlify;

    @Value("${spring.front.origin-domain}")
    private String frontOriginDomain;

    @Value("${spring.front.origin-subdomain}")
    private String frontOriginSubdomain;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // cors를 적용할 spring서버의 url 패턴.
                .allowedOrigins(frontOriginNetlify, frontOriginDomain, frontOriginSubdomain)
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true)
                .exposedHeaders("Set-Cookie", "Authorization");
    }
}

