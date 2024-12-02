package com.example.areumariserver.jwt;

import com.example.areumariserver.dto.JwtToken;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final Key key; // Secret Key 객체

    // application.yml에서 secret 값을 가져와 key 초기화
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Member 정보를 가지고 AccessToken과 RefreshToken을 생성하는 메서드
     */
    public JwtToken generateToken(Authentication authentication) {
        // 권한 정보 추출
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        // Access Token 생성 (24시간 유효)
        Date accessTokenExpiresIn = new Date(now + 86400000);
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("auth", authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성 (7일 유효)
        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())  // 사용자 이름 추가
                .claim("auth", authorities)  // 권한 정보 추가
                .setExpiration(new Date(now + 604800000)) // 7 * 24 * 60 * 60 * 1000
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtToken.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * JWT 토큰을 복호화하여 토큰 정보를 추출하고, Authentication 객체를 생성
     */
    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        // 권한 정보가 없을 경우 예외 발생
        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 추출
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // UserDetails 객체 생성 후 Authentication 반환
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * 토큰 유효성을 검증하는 메서드
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
            return true; // 만료된 토큰도 유효한 것으로 처리
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    /**
     * Access Token에서 Claims를 파싱하는 메서드
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * Refresh Token에서 Authentication 객체를 생성하는 메서드
     */
    public Authentication getAuthenticationFromRefreshToken(String token) {
        Claims claims = parseClaims(token); // 토큰에서 claims 추출

        // username 추출
        String username = claims.getSubject();

        // 권한 정보 추출
        String authoritiesStr = (String) claims.get("auth");

        // 권한 정보가 없을 경우 기본값 설정
        Collection<? extends GrantedAuthority> authorities =
                authoritiesStr != null ?
                        Arrays.stream(authoritiesStr.split(","))
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList()) :
                        Collections.emptyList();

        // UserDetails 객체 생성
        UserDetails principal = new User(username, "", authorities);

        // Authentication 객체 반환
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}