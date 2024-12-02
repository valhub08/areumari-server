package com.example.areumariserver.controller;

import com.example.areumariserver.dto.JwtToken;
import com.example.areumariserver.dto.RefreshTokenRequest;
import com.example.areumariserver.service.MemberServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class AuthController {
    private final MemberServiceImpl memberService;

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtToken> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        JwtToken jwtToken = memberService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(jwtToken);
    }
}