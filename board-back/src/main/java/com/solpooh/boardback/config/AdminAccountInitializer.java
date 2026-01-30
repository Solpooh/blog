package com.solpooh.boardback.config;

import com.solpooh.boardback.entity.UserEntity;
import com.solpooh.boardback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 관리자 계정 정보 (환경 변수 또는 application.yaml에서 주입)
    @Value("${admin.account.email}")
    private String adminEmail;

    @Value("${admin.account.password}")
    private String adminPassword;

    @Value("${admin.account.nickname}")
    private String adminNickname;

    private static final String ADMIN_TEL = "01000000000";
    private static final String ADMIN_ADDRESS = "Seoul";

    @Override
    public void run(String... args) throws Exception {
        // 관리자 계정이 이미 존재하는지 확인
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("관리자 계정이 이미 존재합니다: {}", adminEmail);
            return;
        }

        // 관리자 계정 생성
        UserEntity adminUser = UserEntity.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .nickname(adminNickname)
                .telNumber(ADMIN_TEL)
                .address(ADMIN_ADDRESS)
                .addressDetail("Admin Office")
                .agreedPersonal(true)
                .role(UserEntity.Role.ADMIN)
                .build();

        userRepository.save(adminUser);
        log.info("✅ 관리자 계정이 생성되었습니다.");
        log.info("   - Email: {}", adminEmail);
        log.info("   - Password: {}", adminPassword);
        log.info("   - Role: ADMIN");
    }
}
