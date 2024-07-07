package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.response.user.GetSignInUserResponseDto;
import com.solpooh.boardback.dto.response.user.GetUserResponseDto;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<? super GetUserResponseDto> getUser(String email);
    ResponseEntity<? super GetSignInUserResponseDto> getSignInUser(String email);
}
