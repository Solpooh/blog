package com.solpooh.boardback.controller;

import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.dto.request.auth.SignInRequest;
import com.solpooh.boardback.dto.request.auth.SignUpRequest;
import com.solpooh.boardback.dto.response.ResponseDto;
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
    public ResponseDto<SignUpResponse> signUp(
            @RequestBody @Valid SignUpRequest requestBody
    ) {
        authService.signUp(requestBody);
        return ResponseDto.of(ResponseApi.SUCCESS);
    }

    @PostMapping("/sign-in")
    public ResponseDto<SignInResponse> signIn(
            @RequestBody @Valid SignInRequest requestBody
    ) {
        SignInResponse response = authService.signIn(requestBody);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }
}
