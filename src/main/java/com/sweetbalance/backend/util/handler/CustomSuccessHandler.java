package com.sweetbalance.backend.util.handler;

import com.sweetbalance.backend.dto.identity.CustomOAuth2User;
import com.sweetbalance.backend.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${spring.front.origin-https}")
    private String frontOriginHttps;

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

        addSecureCookie(response, "refresh", refreshToken);
        response.sendRedirect(frontOriginHttps+"/?refresh="+refreshToken);
    }

    private void addSecureCookie(HttpServletResponse response, String name, String value) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(false)
                .secure(true)
                .path("/")
                .maxAge(24 * 60 * 60) // 24시간
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
