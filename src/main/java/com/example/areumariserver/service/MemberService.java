package com.example.areumariserver.service;

import com.example.areumariserver.dto.JwtToken;
import com.example.areumariserver.dto.MemberDto;
import com.example.areumariserver.dto.SignUpDto;

public interface MemberService {
    JwtToken signIn(String username, String password);
    MemberDto signUp(SignUpDto signUpDto); // 회원가입 메서드 추가


}