package com.example.areumariserver.config;

import com.example.areumariserver.jwt.JwtAuthFilter;
import com.example.areumariserver.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // HTTP Basic 인증 비활성화 (JWT 사용)
                .httpBasic(httpBasic -> httpBasic.disable())
                // CSRF 비활성화 (JWT 사용)
                .csrf(csrf -> csrf.disable())
                // 세션 정책을 STATELESS로 설정 (JWT 사용)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 엔드포인트 별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/members/sign-up", "/members/sign-in","/members/refresh-token").permitAll() // 인증 없이 접근 허용
                        .requestMatchers("/posts/**").authenticated()
                        .anyRequest().authenticated() // 나머지는 인증 필요
                )
                // JWT 인증 필터 추가
                .addFilterBefore(new JwtAuthFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Spring Security에서 제공하는 권장 PasswordEncoder
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

