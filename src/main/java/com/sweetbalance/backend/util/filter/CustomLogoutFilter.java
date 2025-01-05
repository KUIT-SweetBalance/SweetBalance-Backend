package com.sweetbalance.backend.util.filter;

import com.sweetbalance.backend.util.InnerFilterResponseSender;
import com.sweetbalance.backend.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

// 프론트엔드측 : 로컬 스토리지에 존재하는 Access 토큰 삭제 및 서버측 로그아웃 경로로 Refresh 토큰 전송
// 추가 가능 로직 : 모든 계정에서 로그아웃 구현시 username 기반으로 모든 Refresh 토큰 삭제도 가능 - 우리 서비스엔 맞지 않음
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;

    public CustomLogoutFilter(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String requestUri = request.getRequestURI();

        if (!requestUri.matches("^/api/auth/sign-out$")) {

            filterChain.doFilter(request, response);
            return;
        }
        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {

            filterChain.doFilter(request, response);
            return;
        }

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {

            InnerFilterResponseSender.sendInnerResponse(response, 400, 999,
                    "쿠키 값 미설정", null);
            return;
        }

        String refresh = null;
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refresh")) {

                refresh = cookie.getValue();
                break;
            }
        }

        if (refresh == null) {

            InnerFilterResponseSender.sendInnerResponse(response, 400, 999,
                    "리프레시 토큰 미설정", null);
            return;
        }

        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            InnerFilterResponseSender.sendInnerResponse(response, 400, 999,
                    "리프레시 토큰 만료", null);
            return;
        } catch (JwtException e) {

            InnerFilterResponseSender.sendInnerResponse(response, 400, 999,
                    "유효하지 않은 토큰", null);
            return;
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String tokenType = jwtUtil.getTokenType(refresh);
        if (!tokenType.equals("refresh")) {

            InnerFilterResponseSender.sendInnerResponse(response, 400, 999,
                    "토큰 타입 미일치", null);
            return;
        }

        //DB에 저장되어 있는지 확인
        if (!jwtUtil.isRefreshExist(refresh)) {

            InnerFilterResponseSender.sendInnerResponse(response, 400, 999,
                    "사용이 제한된 리프레시 토큰", null);
            return;
        }

        //Refresh 토큰 DB에서 제거
        jwtUtil.deleteRefreshEntity(refresh);

        //Refresh 토큰 Cookie 값 0
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);

        InnerFilterResponseSender.sendInnerResponse(response, 200, 0,
                "로그아웃 성공", null);
    }
}