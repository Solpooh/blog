package com.solpooh.boardback.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
public record SignInRequest(
    @NotBlank
    String email,
    @NotBlank
    String password
) { }
