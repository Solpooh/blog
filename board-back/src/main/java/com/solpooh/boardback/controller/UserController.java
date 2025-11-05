package com.solpooh.boardback.controller;

import com.solpooh.boardback.dto.request.user.PatchNicknameRequest;
import com.solpooh.boardback.dto.request.user.PatchProfileImageRequest;
import com.solpooh.boardback.dto.response.user.GetUserResponse;
import com.solpooh.boardback.dto.response.user.PatchNicknameResponse;
import com.solpooh.boardback.dto.response.user.PatchProfileImageResponse;
import com.solpooh.boardback.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/{email}")
    public GetUserResponse getUser(
            @PathVariable("email") String email
    ) {
        return userService.getUser(email);
    }
    @GetMapping("")
    public GetUserResponse getSignInUser(
            @AuthenticationPrincipal String email
    ) {
        return userService.getSignInUser(email);
    }
    @PatchMapping("/nickname")
    public PatchNicknameResponse patchNickname(
            @RequestBody @Valid PatchNicknameRequest requestBody,
            @AuthenticationPrincipal String email
    ) {
        return userService.patchNickname(requestBody, email);
    }
    @PatchMapping("/profile-image")
    public PatchProfileImageResponse patchProfileImage(
            @RequestBody @Valid PatchProfileImageRequest requestBody,
            @AuthenticationPrincipal String email
    ) {
        return userService.patchProfileImage(requestBody, email);
    }
}