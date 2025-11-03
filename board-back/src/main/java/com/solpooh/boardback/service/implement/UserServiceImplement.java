package com.solpooh.boardback.service.implement;

import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.UserConverter;
import com.solpooh.boardback.dto.request.user.PatchNicknameRequest;
import com.solpooh.boardback.dto.request.user.PatchProfileImageRequest;
import com.solpooh.boardback.dto.response.user.GetUserResponse;
import com.solpooh.boardback.dto.response.user.PatchNicknameResponse;
import com.solpooh.boardback.dto.response.user.PatchProfileImageResponse;
import com.solpooh.boardback.entity.UserEntity;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.repository.UserRepository;
import com.solpooh.boardback.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImplement implements UserService {
    private final UserRepository userRepository;

    @Override
    public GetUserResponse getUser(String email) {

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_USER));

        return UserConverter.toResponse(userEntity);
    }

    @Override
    public GetUserResponse getSignInUser(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_USER));

        return UserConverter.toResponse(userEntity);
    }

    @Override
    public PatchNicknameResponse patchNickname(PatchNicknameRequest dto, String email) {

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_USER));

        if (userRepository.existsByNickname(dto.nickname()))
            throw new CustomException(ResponseApi.DUPLICATE_NICKNAME);

        // 닉네임 변경
        userEntity.setNickname(dto.nickname());
        userRepository.save(userEntity);

        return new PatchNicknameResponse();
    }

    @Override
    public PatchProfileImageResponse patchProfileImage(PatchProfileImageRequest dto, String email) {

        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ResponseApi.NOT_EXISTED_USER));

        // 프로필 사진 변경
        userEntity.setProfileImage(dto.profileImage());
        userRepository.save(userEntity);

        return new PatchProfileImageResponse();
    }
}
