package com.solpooh.boardback.converter;

import com.solpooh.boardback.dto.request.auth.SignUpRequest;
import com.solpooh.boardback.entity.UserEntity;

public class AuthConverter {
    private AuthConverter(){}

    // Builder로 선택적 초기화
    public static UserEntity toEntity(SignUpRequest dto) {
        return UserEntity.builder()
                .email(dto.email())
                .password(dto.password())
                .nickname(dto.nickname())
                .telNumber(dto.telNumber())
                .address(dto.address())
                .addressDetail(dto.addressDetail())
                .agreedPersonal(dto.agreedPersonal())
                .build();
    }

}
