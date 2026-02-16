package com.solpooh.boardback.config;

import com.solpooh.boardback.filter.JwtAuthenticationFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;

@Configuration
@EnableWebSecurity // WebSecurity에 대한 config
@EnableMethodSecurity // 메서드 레벨 보안 활성화
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    protected SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                ) // CORS 설정 (프론트/백엔드)
                .csrf(CsrfConfigurer::disable) // CSRF 보호를 비활성화
                .httpBasic(HttpBasicConfigurer::disable) // HTTP Basic 인증 비활성화
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 사용하지 않음 (stateless)
                )
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/", "/api/v1/auth/**", "/api/v1/search/**", "/file/**", "/api/v1/channel/**", "/api/v1/video/**", "/actuator/**").permitAll()
                        // GET 메서드 사용하는 것들에는 인증X
                        .requestMatchers(HttpMethod.GET, "/api/v1/board/**", "/api/v1/user/*", "/api/v1/channel/**").permitAll()
                        // 관리자 API는 ADMIN 권한 필요
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(new FailedAuthenticationEntryPoint())
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 모든 출처에서의 요청을 허용
        configuration.addAllowedOrigin("*");
        // 모든 HTTP 메서드를 허용 (GET, POST, PUT, DELETE 등)
        configuration.addAllowedMethod("*");
        // 모든 요청 헤더를 허용
        configuration.addAllowedHeader("*");
        // 응답 헤더 중 클라이언트에게 노출할 헤더를 설정
        configuration.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 각각의 pattern 에 대해서도 적용가능
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

class FailedAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 인증 실패시 반환 코드
        response.getWriter().write("{\"code\": \"AF\", \"message\": \"Authorization Failed.\"}");
    }
}

class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 권한 부족 시 반환 코드
        response.getWriter().write("{\"code\": \"NP\", \"message\": \"No Permission. Admin role required.\"}");
    }
}