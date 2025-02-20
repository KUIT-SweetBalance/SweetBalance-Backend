package com.sweetbalance.backend.util;

import com.sweetbalance.backend.entity.RefreshEntity;
import com.sweetbalance.backend.repository.refresh.RefreshTokenRepository;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JWTUtil {

    private SecretKey secretKey;
    private final RefreshTokenRepository refreshTokenRepository;

    private final long accessTokenExpirationMs = 1000L * 60 * 30; // 30 minutes
    private final long refreshTokenExpirationMs = 3600000L * 24 * 14; // 14 days

    @Autowired
    public JWTUtil(@Value("${spring.jwt.secret}")String secret, RefreshTokenRepository refreshTokenRepository) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public Long getUserId(String token) {
        String rawUserId = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("sub", String.class);
        return Long.valueOf(rawUserId);
    }

    public String getEmail(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public String getUserType(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userType", String.class);
    }

    public String getTokenType(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("tokenType", String.class);
    }

    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String generateBasicAccessToken(Long userId, String email, String role) {
        return Jwts.builder()
                .claim("sub", userId.toString())
                .claim("userType", "basic")
                .claim("tokenType", "access")
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String generateSocialAccessToken(Long userId, String email, String role) {
        return Jwts.builder()
                .claim("sub", userId.toString())
                .claim("userType", "social")
                .claim("tokenType", "access")
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String generateBasicRefreshToken(Long userId, String email, String role) {
        String refreshToken = Jwts.builder()
                .claim("sub", userId.toString())
                .claim("userType", "basic")
                .claim("tokenType", "refresh")
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(secretKey)
                .compact();
        addRefreshEntity(email, refreshToken);
        return refreshToken;
    }

    public String generateSocialRefreshToken(Long userId, String email, String role) {
        String refreshToken = Jwts.builder()
                .claim("sub", userId.toString())
                .claim("userType", "basic")
                .claim("tokenType", "refresh")
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(secretKey)
                .compact();
        addRefreshEntity(email, refreshToken);
        return refreshToken;
    }

    public boolean isRefreshExist(String refresh){
        return refreshTokenRepository.existsByRefresh(refresh);
    }

    public void deleteRefreshEntity(String refresh){
        refreshTokenRepository.deleteByRefresh(refresh);
    }

    private void addRefreshEntity(String email, String refresh) {

        LocalDateTime expirationDateTime = LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setEmail(email);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(expirationDateTime);

        refreshTokenRepository.save(refreshEntity);
    }

    public void setRefreshCookie(HttpServletResponse response, String refreshTokenString) {
        ResponseCookie cookie = ResponseCookie.from("refresh", refreshTokenString)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(refreshTokenExpirationMs / 1000)
                //.domain(".sweetbalance.site")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void resetRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                //.domain(".sweetbalance.site")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
