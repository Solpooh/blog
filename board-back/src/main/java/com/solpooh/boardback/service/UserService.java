package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.request.user.PatchNicknameRequestDto;
import com.solpooh.boardback.dto.request.user.PatchProfileImageRequestDto;
import com.solpooh.boardback.dto.response.user.GetSignInUserResponseDto;
import com.solpooh.boardback.dto.response.user.GetUserResponseDto;
import com.solpooh.boardback.dto.response.user.PatchNicknameResponseDto;
import com.solpooh.boardback.dto.response.user.PatchProfileImageResponseDto;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<? super GetUserResponseDto> getUser(String email);
    ResponseEntity<? super GetSignInUserResponseDto> getSignInUser(String email);
    ResponseEntity<? super PatchNicknameResponseDto> patchNickname(PatchNicknameRequestDto dto, String email);
    ResponseEntity<? super PatchProfileImageResponseDto> patchProfileImage(PatchProfileImageRequestDto dto, String email);
}
