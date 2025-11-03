package com.solpooh.boardback.dto.request.user;

import jakarta.validation.constraints.NotBlank;

public record PatchNicknameRequest(
        @NotBlank
        String nickname
) { }
