package com.solpooh.boardback.service.implement;

import com.solpooh.boardback.common.ResponseApi;
import com.solpooh.boardback.converter.AuthConverter;
import com.solpooh.boardback.dto.request.auth.SignInRequest;
import com.solpooh.boardback.dto.request.auth.SignUpRequest;

import com.solpooh.boardback.dto.response.auth.SignInResponse;
import com.solpooh.boardback.dto.response.auth.SignUpResponse;
import com.solpooh.boardback.entity.UserEntity;
import com.solpooh.boardback.exception.CustomException;
import com.solpooh.boardback.provider.JwtProvider;
import com.solpooh.boardback.repository.UserRepository;
import com.solpooh.boardback.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImplement implements AuthService {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public SignUpResponse signUp(SignUpRequest dto) {
        // 패스워드 암호화
        UserEntity userEntity = AuthConverter.toEntity(dto);
        String encodedPassword = passwordEncoder.encode(dto.password());
        userEntity.setPassword(encodedPassword);
        userRepository.save(userEntity);

        return new SignUpResponse();
    }

    @Override
    public SignInResponse signIn(SignInRequest dto) {
        // 이메일 비교(UserEntity)
        UserEntity userEntity = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new CustomException(ResponseApi.SIGN_IN_FAIL));

        // 비밀번호 비교 (평문 <-> 암호)
        if (!passwordEncoder.matches(dto.password(), userEntity.getPassword()))
            throw new CustomException(ResponseApi.SIGN_IN_FAIL);

        String token = jwtProvider.create(dto.email());

        return new SignInResponse(token, 3600);
    }
}
