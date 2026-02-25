package com.uplus.crm.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpireMs;
    private final long refreshTokenExpireMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expire-ms}") long accessTokenExpireMs,
            @Value("${jwt.refresh-token-expire-ms}") long refreshTokenExpireMs) {

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpireMs = accessTokenExpireMs;
        this.refreshTokenExpireMs = refreshTokenExpireMs;
    }

    // Access Token 생성
    public String generateAccessToken(Integer empId, String loginId) {
        return Jwts.builder()
                .subject(String.valueOf(empId))
                .claim("loginId", loginId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpireMs))
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(Integer empId) {
        return Jwts.builder()
                .subject(String.valueOf(empId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpireMs))
                .signWith(secretKey)
                .compact();
    }

    // Access Token 만료 시각 반환
    public LocalDateTime getAccessTokenExpiredAt() {
        return LocalDateTime.now().plusSeconds(accessTokenExpireMs / 1000);
    }

    // Refresh Token 만료 시각 반환
    public LocalDateTime getRefreshTokenExpiredAt() {
        return LocalDateTime.now().plusSeconds(refreshTokenExpireMs / 1000);
    }

    // 토큰에서 empId 추출
    public Integer getEmpId(String token) {
        return Integer.parseInt(parseClaims(token).getSubject());
    }

    // 토큰 유효성 검증
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Claims 파싱
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}