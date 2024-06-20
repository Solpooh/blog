package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.request.auth.SignUpRequestDto;
import com.solpooh.boardback.dto.response.auth.SignUpResponseDto;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto);
}
