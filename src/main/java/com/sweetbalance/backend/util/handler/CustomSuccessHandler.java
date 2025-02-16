package com.sweetbalance.backend.util.handler;

import com.sweetbalance.backend.dto.identity.CustomOAuth2User;
import com.sweetbalance.backend.util.JWTUtil;
import jakarta.servlet.ServletException;
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

    @Value("${spring.front.origin-deployed}")
    private String frontOriginDeployed;

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
        String email = customUserDetails.getEmail();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String refreshToken = jwtUtil.generateSocialRefreshToken(userId, email, role);
        boolean isNew = customUserDetails.isNewUser();

        addSecureCookie(response, "new", String.valueOf(isNew));
        addSecureCookie(response, "refresh", refreshToken);

        response.sendRedirect(frontOriginDeployed+"/oauth2_redirect?new="+isNew+"&refresh="+refreshToken);
    }

    // 프론트 측 배포이후 공통된 서브 도메인으로 cookie domain 설정해야만 서드파티 쿠키 사용 가능
    private void addSecureCookie(HttpServletResponse response, String name, String value) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(false)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(24 * 60 * 60)
                //.domain(".nip.io")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
