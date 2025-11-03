package com.solpooh.boardback.service;

import com.solpooh.boardback.dto.request.user.PatchNicknameRequest;
import com.solpooh.boardback.dto.request.user.PatchProfileImageRequest;
import com.solpooh.boardback.dto.response.user.GetUserResponse;
import com.solpooh.boardback.dto.response.user.PatchNicknameResponse;
import com.solpooh.boardback.dto.response.user.PatchProfileImageResponse;

public interface UserService {
    GetUserResponse getUser(String email);
    GetUserResponse getSignInUser(String email);
    PatchNicknameResponse patchNickname(PatchNicknameRequest dto, String email);
    PatchProfileImageResponse patchProfileImage(PatchProfileImageRequest dto, String email);
}
