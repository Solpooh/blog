package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.request.auth.SignInRequest;
import com.solpooh.boardback.dto.request.auth.SignUpRequest;
import com.solpooh.boardback.dto.response.auth.SignInResponse;
import com.solpooh.boardback.dto.response.auth.SignUpResponse;
import com.solpooh.boardback.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @PostMapping("/sign-up")
    public SignUpResponse signUp(
            @RequestBody @Valid SignUpRequest requestBody
    ) {
        return authService.signUp(requestBody);
    }

    @PostMapping("/sign-in")
    public SignInResponse signIn(
            @RequestBody @Valid SignInRequest requestBody
    ) {
        return authService.signIn(requestBody);
    }
}