package com.sweetbalance.backend.util.filter;

import com.sweetbalance.backend.dto.identity.AuthUserDTO;
import com.sweetbalance.backend.dto.identity.CustomOAuth2User;
import com.sweetbalance.backend.dto.identity.CustomUserDetails;
import com.sweetbalance.backend.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Autowired
    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            
        //특정 엔드포인트 요청에 대해 필터를 넘어가도록 따로 설정도 가능함 (정규표현식 사용)
//        String requestUri = request.getRequestURI();
//        if (requestUri.matches("/api/users/sign-up") || requestUri.matches("/api/users/sign-in")) {
//            filterChain.doFilter(request, response);
//            return;
//        }

        String authorization= request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null");
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authorization.split(" ")[1];

        /*
         * 하단 응답 메세지 및 상태코드는 프론트와 조정이 필요함
         */
        
        //토큰 소멸 시간 검증
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String tokenType = jwtUtil.getTokenType(accessToken);

        if (!tokenType.equals("access")) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Long userId = jwtUtil.getUserId(accessToken);
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);
        String userType = jwtUtil.getUserType(accessToken);

        AuthUserDTO authUserDTO = new AuthUserDTO();
        authUserDTO.setUserId(userId);
        authUserDTO.setUsername(username);
        authUserDTO.setRole(role);

        Authentication authToken = null;

        if(userType.equals("basic")){
            CustomUserDetails customUserDetails = new CustomUserDetails(authUserDTO);

            //스프링 시큐리티 인증 토큰 생성
            authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        }

        if(userType.equals("social")){
            //UserDetails에 회원 정보 객체 담기
            CustomOAuth2User customOAuth2User = new CustomOAuth2User(authUserDTO);
            //스프링 시큐리티 인증 토큰 생성
            authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        }

        //세션에 사용자 등록, 일시적 세션을 통해 요청 시 로그인 된 형태로 변경하기 위해 SecurityContextHolder 에 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}