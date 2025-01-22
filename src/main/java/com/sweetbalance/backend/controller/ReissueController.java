package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReissueController {
    private final JWTUtil jwtUtil;

    public ReissueController(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/api/auth/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {

            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 999, "쿠키 값 미설정")
            );
        }

        String refresh = null;
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refresh")) {

                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {

            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 999, "리프레시 토큰 미설정")
            );
        }

        //expired check
        try {

            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 999, "리프레시 토큰 만료")
            );
        } catch (JwtException e) {

            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 999, "유효하지 않은 토큰")
            );
        }

        // 토큰이 refresh 인지 확인 (발급 시 페이로드에 명시)
        String tokenType = jwtUtil.getTokenType(refresh);

        if (!tokenType.equals("refresh")) {

            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 999, "토큰 타입 미일치")
            );
        }

        //DB에 저장되어 있는지 확인
        if (!jwtUtil.isRefreshExist(refresh)) {

            return ResponseEntity.status(400).body(
                    DefaultResponseDTO.error(400, 999, "사용이 제한된 리프레시 토큰")
            );
        }

        Long userId = jwtUtil.getUserId(refresh);
        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        String userType = jwtUtil.getUserType(refresh);
        String newAccessToken = null;
        String newRefreshToken = null;

        if(userType.equals("basic")){
            newAccessToken = jwtUtil.generateBasicAccessToken(userId, username, role);
            newRefreshToken = jwtUtil.generateBasicRefreshToken(userId, username, role);
        }
        if(userType.equals("social")){
            newAccessToken = jwtUtil.generateSocialAccessToken(userId, username, role);
            newRefreshToken = jwtUtil.generateSocialRefreshToken(userId, username, role);
        }
        jwtUtil.deleteRefreshEntity(refresh);

        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(createCookie("refresh", newRefreshToken));

        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("토큰 재발급 성공", null)
        );
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setPath("/");        // 쿠키가 보일 위치 설정
        //cookie.setSecure(true);   // HTTPS 에서만 쿠키를 사용할 수 있도록 설정
        //cookie.setHttpOnly(true);   // JavaScript 쿠키 조작 불가능

        return cookie;
    }
}
