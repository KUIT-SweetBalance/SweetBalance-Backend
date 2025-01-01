package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
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

        //get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {

            //response status code
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            //response status code
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급 시 페이로드에 명시)
        String tokenType = jwtUtil.getTokenType(refresh);

        if (!tokenType.equals("refresh")) {
            //response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        //DB에 저장되어 있는지 확인
        if (!jwtUtil.isRefreshExist(refresh)) {

            //response body
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
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

        //response
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(createCookie("refresh", newRefreshToken));

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);   // HTTPS 에서만 쿠키를 사용할 수 있도록 설정
        cookie.setPath("/");        // 쿠키가 보일 위치 설정
        cookie.setHttpOnly(true);   // JavaScript 쿠키 조작 불가능

        return cookie;
    }
}
