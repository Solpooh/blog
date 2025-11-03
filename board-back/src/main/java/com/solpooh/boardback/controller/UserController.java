package com.solpooh.boardback.controller;

import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.dto.request.user.PatchNicknameRequest;
import com.solpooh.boardback.dto.request.user.PatchProfileImageRequest;
import com.solpooh.boardback.dto.response.ResponseDto;
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
    public ResponseDto<GetUserResponse> getUser(
            @PathVariable("email") String email
    ) {
        GetUserResponse response = userService.getUser(email);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }
    @GetMapping("")
    public ResponseDto<GetUserResponse> getSignInUser(
            @AuthenticationPrincipal String email
    ) {
        GetUserResponse response = userService.getSignInUser(email);
        return ResponseDto.of(ResponseApi.SUCCESS, response);
    }
    @PatchMapping("/nickname")
    public ResponseDto<PatchNicknameResponse> patchNickname(
            @RequestBody @Valid PatchNicknameRequest requestBody,
            @AuthenticationPrincipal String email
    ) {
        userService.patchNickname(requestBody, email);
        return ResponseDto.of(ResponseApi.SUCCESS);
    }
    @PatchMapping("/profile-image")
    public ResponseDto<PatchProfileImageResponse> patchProfileImage(
            @RequestBody @Valid PatchProfileImageRequest requestBody,
            @AuthenticationPrincipal String email
    ) {
        userService.patchProfileImage(requestBody, email);
        return ResponseDto.of(ResponseApi.SUCCESS);
    }
}
