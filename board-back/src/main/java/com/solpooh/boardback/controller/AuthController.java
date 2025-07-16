package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.request.auth.SignUpRequestDto;
import com.solpooh.boardback.dto.request.auth.SignInRequestDto;
import com.solpooh.boardback.dto.response.auth.SignInResponseDto;
import com.solpooh.boardback.dto.response.auth.SignUpResponseDto;
import com.solpooh.boardback.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/sign-up")
    public ResponseEntity<? super SignUpResponseDto> signUp(
            @RequestBody @Valid SignUpRequestDto requestBody
    ) {
        return authService.signUp(requestBody);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<? super SignInResponseDto> signIn(
            @RequestBody @Valid SignInRequestDto requestBody
    ) {
        return authService.signIn(requestBody);
    }
}
