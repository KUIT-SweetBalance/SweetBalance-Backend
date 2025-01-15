package com.sweetbalance.backend.util.handler;

import com.sweetbalance.backend.dto.identity.CustomOAuth2User;
import com.sweetbalance.backend.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Autowired
    public CustomSuccessHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        Long userId = customUserDetails.getUserId();
        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String refreshToken = jwtUtil.generateSocialRefreshToken(userId, username, role);

        //refresh 토큰 발급을 통해 클라이언트가 reissue 할 수 있도록 함
        response.addCookie(createCookie("refresh", refreshToken));
        response.sendRedirect("http://localhost:5173/?refresh="+refreshToken);    // 프론트 측 특정 URL
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setPath("/");        // 쿠키가 보일 위치 설정
        //cookie.setHttpOnly(true);   // JavaScript 쿠키 조작 불가능
        //cookie.setSecure(true);   // HTTPS 에서만 쿠키를 사용할 수 있도록 설정

        return cookie;
    }
}
