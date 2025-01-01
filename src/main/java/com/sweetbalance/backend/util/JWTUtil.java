package com.sweetbalance.backend.util;

import com.sweetbalance.backend.entity.RefreshEntity;
import com.sweetbalance.backend.repository.RefreshRepository;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    private SecretKey secretKey;
    private final RefreshRepository refreshRepository;

    private final long accessTokenExpirationMs = 3600000L; // 1 hour
    private final long refreshTokenExpirationMs = 3600000L * 24 * 30; // 30 days

    @Autowired
    public JWTUtil(@Value("${spring.jwt.secret}")String secret, RefreshRepository refreshRepository) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.refreshRepository = refreshRepository;
    }

    public Long getUserId(String token) {
        String rawUserId = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("sub", String.class);
        return Long.valueOf(rawUserId);
    }

    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
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

    public String generateBasicAccessToken(Long userId, String username, String role) {
        return Jwts.builder()
                .claim("sub", userId.toString())
                .claim("userType", "basic")
                .claim("tokenType", "access")
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String generateSocialAccessToken(Long userId, String username, String role) {
        return Jwts.builder()
                .claim("sub", userId.toString())
                .claim("userType", "social")
                .claim("tokenType", "access")
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(secretKey)
                .compact();
    }

    public String generateBasicRefreshToken(Long userId, String username, String role) {
        String refreshToken = Jwts.builder()
                .claim("sub", userId.toString())
                .claim("userType", "basic")
                .claim("tokenType", "refresh")
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(secretKey)
                .compact();
        addRefreshEntity(username, refreshToken);
        return refreshToken;
    }

    public String generateSocialRefreshToken(Long userId, String username, String role) {
        String refreshToken = Jwts.builder()
                .claim("sub", userId.toString())
                .claim("userType", "basic")
                .claim("tokenType", "refresh")
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(secretKey)
                .compact();
        addRefreshEntity(username, refreshToken);
        return refreshToken;
    }

    public boolean isRefreshExist(String refresh){
        return refreshRepository.existsByRefresh(refresh);
    }

    public void deleteRefreshEntity(String refresh){
        refreshRepository.deleteByRefresh(refresh);
    }

    private void addRefreshEntity(String username, String refresh) {

        Date date = new Date(System.currentTimeMillis() + refreshTokenExpirationMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }
}
