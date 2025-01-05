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

import java.util.Collections;

import static com.sweetbalance.backend.enums.user.Role.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.front.origin}")
    private String frontOrigin;

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

                        configuration.setAllowedOrigins(Collections.singletonList(frontOrigin));
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setMaxAge(3600L);

                        configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

                        return configuration;
                    }
                }));

        //csrf disable
        httpSecurity
                .csrf((auth) -> auth.disable());

        //From 로그인 방식 disable
        httpSecurity
                .formLogin((auth) -> auth.disable());

        //HTTP Basic 인증 방식 disable
        httpSecurity
                .httpBasic((auth) -> auth.disable());
        
        //oauth2
        httpSecurity
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                );

        //경로별 인가 작업
        httpSecurity
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/api/auth/sign-up", "/api/auth/sign-in").permitAll()
                        .requestMatchers("/api/auth/reissue").permitAll()
                        .requestMatchers("/api/auth/sign-out").hasAnyAuthority(ADMIN.getValue(), USER.getValue())
                        .requestMatchers("/api/users/**").hasAnyAuthority(ADMIN.getValue(), USER.getValue())
                        .anyRequest().authenticated())

                .exceptionHandling(customizer -> customizer.authenticationEntryPoint((request, response, authException) -> {

                    InnerFilterResponseSender.sendInnerResponse(response, 500, 0,
                            "서버 오류가 발생했습니다.", null);
                }));

        //LoginFilter 추가 - BASIC
        httpSecurity
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        //CustomLogoutFilter 추가
        httpSecurity
                .addFilterBefore(new CustomLogoutFilter(jwtUtil), LogoutFilter.class);

        //JWTFilter 추가
        httpSecurity
                .addFilterAfter(new JWTFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class);

        //세션 설정 : STATELESS
        httpSecurity
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return httpSecurity.build();
    }
}
