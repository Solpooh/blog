package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.request.SignUpRequestDto;
import com.solpooh.boardback.dto.request.auth.SignInRequestDto;
import com.solpooh.boardback.dto.response.auth.SignInResponseDto;
import com.solpooh.boardback.dto.response.auth.SignUpResponseDto;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto);
    ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto);
}
