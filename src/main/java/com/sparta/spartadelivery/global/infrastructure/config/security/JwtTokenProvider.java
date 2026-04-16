package com.sparta.spartadelivery.global.infrastructure.config.security;

import com.sparta.spartadelivery.global.exception.AppException;
import com.sparta.spartadelivery.global.exception.ErrorCode;
import com.sparta.spartadelivery.user.domain.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String ROLE_CLAIM = "role";
    private static final String USERNAME_CLAIM = "username";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    // JWT 액세스 토큰 생성
    public String generateAccessToken(UserPrincipal userPrincipal) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.accessTokenExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userPrincipal.getId()))
                .claim(USERNAME_CLAIM, userPrincipal.getAccountName())
                .claim(ROLE_CLAIM, userPrincipal.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    // JWT 액세스 토큰에서 사용자 정보 추출
    public TokenPayload getPayload(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new TokenPayload(
                    Long.valueOf(claims.getSubject()),
                    claims.get(USERNAME_CLAIM, String.class),
                    Role.valueOf(claims.get(ROLE_CLAIM, String.class))
            );
        } catch (JwtException | IllegalArgumentException exception) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public record TokenPayload(
            Long userId,
            String username,
            Role role
    ) {
    }
}
