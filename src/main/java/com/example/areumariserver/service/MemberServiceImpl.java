package com.example.areumariserver.service;

import com.example.areumariserver.dto.JwtToken;
import com.example.areumariserver.dto.MemberDto;
import com.example.areumariserver.dto.SignUpDto;
import com.example.areumariserver.jwt.JwtTokenProvider;
import com.example.areumariserver.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import com.example.areumariserver.repository.RefreshTokenRepository; // 추가

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository; // 추가

    @Transactional
    @Override
    public JwtToken signIn(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication =
                authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        // Refresh Token 저장
        refreshTokenRepository.save(username, jwtToken.getRefreshToken());

        return jwtToken;
    }

    @Transactional
    @Override
    public MemberDto signUp(SignUpDto signUpDto) {
        if (memberRepository.existsByUsername(signUpDto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signUpDto.getPassword());
        List<String> roles = new ArrayList<>();
        roles.add("USER");

        return MemberDto.toDto(
                memberRepository.save(signUpDto.toEntity(encodedPassword, roles))
        );
    }

    @Transactional
    public JwtToken refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        String username = refreshTokenRepository.findByToken(refreshToken);
        if (username == null) {
            throw new IllegalArgumentException("Refresh Token이 저장소에 없습니다.");
        }

        Authentication authentication = jwtTokenProvider.getAuthenticationFromRefreshToken(refreshToken);
        JwtToken newToken = jwtTokenProvider.generateToken(authentication);

        refreshTokenRepository.save(username, newToken.getRefreshToken());
        return newToken;
    }
}
