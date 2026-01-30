package com.solpooh.boardback.dto.common;

import com.solpooh.boardback.entity.UserEntity;

public record JwtClaims(
        String email,
        UserEntity.Role role
) {}
