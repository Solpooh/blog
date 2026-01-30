package com.solpooh.boardback.converter;

import com.solpooh.boardback.dto.response.user.GetUserResponse;
import com.solpooh.boardback.entity.UserEntity;

public class UserConverter {
    public static GetUserResponse toResponse(UserEntity userEntity) {
        return new GetUserResponse(
                userEntity.getEmail(),
                userEntity.getNickname(),
                userEntity.getProfileImage(),
                userEntity.getRole().name()
        );
    }
}
