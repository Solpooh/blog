package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.request.user.PatchNicknameRequestDto;
import com.solpooh.boardback.dto.request.user.PatchProfileImageRequestDto;
import com.solpooh.boardback.dto.response.user.GetSignInUserResponseDto;
import com.solpooh.boardback.dto.response.user.GetUserResponseDto;
import com.solpooh.boardback.dto.response.user.PatchNicknameResponseDto;
import com.solpooh.boardback.dto.response.user.PatchProfileImageResponseDto;
import com.solpooh.boardback.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/{email}")
    public ResponseEntity<? super GetUserResponseDto> getUser(
            @PathVariable("email") String email
    ) {
        return userService.getUser(email);
    }
    @GetMapping("")
    public ResponseEntity<? super GetSignInUserResponseDto> getSignInUser(
            @AuthenticationPrincipal String email
    ) {
        return userService.getSignInUser(email);
    }
    @PatchMapping("/nickname")
    public ResponseEntity<? super PatchNicknameResponseDto> patchNickname(
            @RequestBody @Valid PatchNicknameRequestDto requestBody,
            @AuthenticationPrincipal String email
    ) {
        return userService.patchNickname(requestBody, email);
    }
    @PatchMapping("/profile-image")
    public ResponseEntity<? super PatchProfileImageResponseDto> patchProfileImage(
            @RequestBody @Valid PatchProfileImageRequestDto requestBody,
            @AuthenticationPrincipal String email
    ) {
        return userService.patchProfileImage(requestBody, email);
    }
}
