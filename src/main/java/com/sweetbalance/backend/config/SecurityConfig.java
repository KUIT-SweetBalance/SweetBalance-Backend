package com.sweetbalance.backend.config;

import com.sweetbalance.backend.service.CustomOAuth2UserService;
import com.sweetbalance.backend.util.*;
import com.sweetbalance.backend.util.filter.CustomLogoutFilter;
import com.sweetbalance.backend.util.filter.JWTFilter;
import com.sweetbalance.backend.util.filter.LoginFilter;
import com.sweetbalance.backend.util.handler.CustomSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.sweetbalance.backend.enums.user.Role.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.front.origin-netlify}")
    private String frontOriginNetlify;

    @Value("${spring.front.origin-domain}")
    private String frontOriginDomain;

    @Value("${spring.front.origin-subdomain}")
    private String frontOriginSubdomain;

    private final AuthenticationConfiguration authenticationConfiguration;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;

    @Autowired
    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, CustomOAuth2UserService customOAuth2UserService,
                          CustomSuccessHandler customSuccessHandler, JWTUtil jwtUtil) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtUtil = jwtUtil;
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // Role Hierarchy 필요하다면 추가

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{

        //CORS 설정
        httpSecurity
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(List.of(frontOriginNetlify, frontOriginDomain, frontOriginSubdomain));
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setMaxAge(3600L);

                        configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie"));

                        return configuration;
                    }
                }));

        //CSRF(Cross-Site Request Forgery) 보호 기능 비활성화
        httpSecurity
                .csrf((auth) -> auth.disable());

        //Spring Security의 기본 폼 로그인 방식(Username & Password 로그인 폼) 비활성화
        httpSecurity
                .formLogin((auth) -> auth.disable());

        //HTTP Basic 인증 방식 비활성화
        httpSecurity
                .httpBasic((auth) -> auth.disable());
        
        //OAuth2 로그인 설정
        httpSecurity
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                );

        //경로별 인가 작업
        httpSecurity
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/auth/sign-out").hasAnyAuthority(ADMIN.getValue(), USER.getValue())
                        .requestMatchers("/api/auth/withdraw").hasAnyAuthority(ADMIN.getValue(), USER.getValue())
                        .requestMatchers("/api/user/**").hasAnyAuthority(ADMIN.getValue(), USER.getValue())
                        .requestMatchers("/api/beverages/**").hasAnyAuthority(ADMIN.getValue(), USER.getValue())
                        .anyRequest().permitAll())

                .exceptionHandling(customizer -> customizer
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 인증 실패 처리 (401 Unauthorized)
                            InnerFilterResponseSender.sendInnerResponse(response, 401, 999,
                                    "인증이 필요합니다. 로그인해주세요.", null);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // 권한 부족 처리 (403 Forbidden)
                            InnerFilterResponseSender.sendInnerResponse(response, 403, 999,
                                    "권한이 부족합니다.", null);
                        }));

        //LoginFilter 추가 - 일반 로그인
        httpSecurity
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        //CustomLogoutFilter 추가
        httpSecurity
                .addFilterBefore(new CustomLogoutFilter(jwtUtil), LogoutFilter.class);

        //JWTFilter 추가
        httpSecurity
                .addFilterAfter(new JWTFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class);

        //세션을 사용하지 않는 방식(STATELESS)으로 설정
        httpSecurity
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return httpSecurity.build();
    }
}
