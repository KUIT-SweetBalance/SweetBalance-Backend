package com.sweetbalance.backend.util.filter;

import com.sweetbalance.backend.dto.identity.AuthUserDTO;
import com.sweetbalance.backend.dto.identity.CustomOAuth2User;
import com.sweetbalance.backend.dto.identity.CustomUserDetails;
import com.sweetbalance.backend.util.InnerFilterResponseSender;
import com.sweetbalance.backend.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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

        String authorization= request.getHeader("Authorization");

        //Authorization 헤더 검증
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("token null");
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authorization.split(" ")[1];
        
        //토큰 검증
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {

            InnerFilterResponseSender.sendInnerResponse(response, 400, 999,
                    "엑세스 토큰 만료", null);
            return;
        } catch (JwtException e) {

            InnerFilterResponseSender.sendInnerResponse(response, 400, 999,
                    "유효하지 않은 토큰", null);
            return;
        }

        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String tokenType = jwtUtil.getTokenType(accessToken);

        if (!tokenType.equals("access")) {

            InnerFilterResponseSender.sendInnerResponse(response, 400, 999,
                    "토큰 타입 미일치", null);
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

        Authentication authToken = getAuthentication(userType, authUserDTO);

        //세션에 사용자 등록, 일시적 세션을 통해 요청 시 로그인 된 형태로 변경하기 위해 SecurityContextHolder 에 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();

        return requestURI.startsWith("/api/auth/reissue");
    }

    private Authentication getAuthentication(String userType, AuthUserDTO authUserDTO) {
        Authentication authToken = null;

        if(userType.equals("basic")){

            CustomUserDetails customUserDetails = new CustomUserDetails(authUserDTO);

            authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        }

        if(userType.equals("social")){

            CustomOAuth2User customOAuth2User = new CustomOAuth2User(authUserDTO);

            authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        }

        return authToken;
    }
}