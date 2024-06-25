package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.response.user.GetSignInUserResponseDto;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<? super GetSignInUserResponseDto> getSignInUser(String email);
}
