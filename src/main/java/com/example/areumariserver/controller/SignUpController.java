package com.example.areumariserver.controller;

import com.example.areumariserver.dto.MemberDto;
import com.example.areumariserver.dto.SignUpDto;
import com.example.areumariserver.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class SignUpController {

    private final MemberService memberService;

    @PostMapping("/sign-up")
    public ResponseEntity<MemberDto> signUp(@RequestBody SignUpDto signUpDto) {
        MemberDto savedMember = memberService.signUp(signUpDto);
        return ResponseEntity.ok(savedMember);
    }
}