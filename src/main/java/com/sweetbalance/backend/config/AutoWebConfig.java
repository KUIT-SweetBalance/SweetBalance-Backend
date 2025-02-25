package com.sweetbalance.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan
public class AutoWebConfig implements WebMvcConfigurer {

    @Value("${spring.front.origin-http}")
    private String frontOriginHttp;

    @Value("${spring.front.origin-https}")
    private String frontOriginHttps;

    @Value("${spring.front.origin-deployed}")
    private String frontOriginDeployed;

    @Value("${spring.front.origin-subdomain-match-http}")
    private String frontOriginSubdomainMatchHttp;

    @Value("${spring.front.origin-subdomain-match-https}")
    private String frontOriginSubdomainMatchHttps;


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // cors를 적용할 spring서버의 url 패턴.
                .allowedOrigins(frontOriginHttp, frontOriginHttps, frontOriginDeployed, frontOriginSubdomainMatchHttp, frontOriginSubdomainMatchHttps)
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true)
                .exposedHeaders("Set-Cookie", "Authorization");
    }
}

