package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.response.token.TokenPairDTO;
import com.sweetbalance.backend.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReissueController {
    private final JWTUtil jwtUtil;

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
        String email = jwtUtil.getEmail(refresh);
        String role = jwtUtil.getRole(refresh);

        String userType = jwtUtil.getUserType(refresh);
        String newAccessToken = null;
        String newRefreshToken = null;

        if(userType.equals("basic")){
            newAccessToken = jwtUtil.generateBasicAccessToken(userId, email, role);
            newRefreshToken = jwtUtil.generateBasicRefreshToken(userId, email, role);
        }
        if(userType.equals("social")){
            newAccessToken = jwtUtil.generateSocialAccessToken(userId, email, role);
            newRefreshToken = jwtUtil.generateSocialRefreshToken(userId, email, role);
        }
        jwtUtil.deleteRefreshEntity(refresh);

        TokenPairDTO tokens = new TokenPairDTO(newAccessToken, newRefreshToken);
        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("토큰 재발급 성공", tokens)
        );
    }
}
