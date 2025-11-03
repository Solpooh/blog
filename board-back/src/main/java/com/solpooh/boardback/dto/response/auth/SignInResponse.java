package com.solpooh.boardback.dto.response.auth;

public record SignInResponse(
        String token,
        int expirationTime
) { }
