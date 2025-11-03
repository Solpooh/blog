package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.request.auth.SignInRequest;
import com.solpooh.boardback.dto.request.auth.SignUpRequest;
import com.solpooh.boardback.dto.response.auth.SignInResponse;
import com.solpooh.boardback.dto.response.auth.SignUpResponse;

public interface AuthService {
    SignUpResponse signUp(SignUpRequest dto);
    SignInResponse signIn(SignInRequest dto);
}
