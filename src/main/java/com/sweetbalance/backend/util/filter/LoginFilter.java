package com.sweetbalance.backend.util.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sweetbalance.backend.dto.identity.CustomUserDetails;
import com.sweetbalance.backend.dto.response.token.TokenPairDTO;
import com.sweetbalance.backend.util.InnerFilterResponseSender;
import com.sweetbalance.backend.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final JWTUtil jwtUtil;

    @Autowired
    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.objectMapper = new ObjectMapper();
        this.jwtUtil = jwtUtil;
        this.setFilterProcessesUrl("/api/auth/sign-in");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String email;
        String password;

        // JSON 형식 요청 받아오기
        try {
            String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            Map<String, String> jsonRequest = objectMapper.readValue(body, Map.class);

            email = jsonRequest.get("email");
            password = jsonRequest.get("password");
        } catch (IOException e) {
            throw new AuthenticationException("Failed to parse authentication request body") {};
        }

        //스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);

        //token에 담은 검증을 위한 AuthenticationManager로 전달
        return authenticationManager.authenticate(authToken);
    }

    //로그인 성공시 실행하는 메소드 (JWT 발급)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = customUserDetails.getUserId();
        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.generateBasicAccessToken(userId, username, role);
        String refreshToken = jwtUtil.generateBasicRefreshToken(userId, username, role);

        TokenPairDTO tokens = new TokenPairDTO(accessToken, refreshToken);
        InnerFilterResponseSender.sendInnerResponse(response, 200, 0,
                "로그인 성공, 토큰 발급 성공", tokens);
    }

    //로그인 실패시 실행하는 메소드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {

        InnerFilterResponseSender.sendInnerResponse(response, 400, 999,
                "로그인 인증 실패", null);
    }
}
