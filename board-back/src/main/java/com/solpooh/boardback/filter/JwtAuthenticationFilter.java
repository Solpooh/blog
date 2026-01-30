package com.solpooh.boardback.filter;

import com.solpooh.boardback.dto.common.JwtClaims;
import com.solpooh.boardback.provider.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static String parseBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);

        boolean hasAuthorization = StringUtils.hasText(authorization);
        if (!hasAuthorization) return null;

        // Bearer 인증 방식이 맞는지
        boolean isBearer = authorization.startsWith(BEARER_PREFIX);
        if (!isBearer) return null;

        String token = authorization.substring(7);

        return token;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            String token = parseBearerToken(request);
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // JWT 클레임에서 email과 role 추출
            JwtClaims jwtClaims = jwtProvider.validate(token);
            if (jwtClaims == null) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtClaims.email();
            // role을 Spring Security의 GrantedAuthority로 변환 (ROLE_ prefix 필요)
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + jwtClaims.role().name())
            );

            // Spring Security의 인증 객체 생성 (권한 정보 포함)
            AbstractAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            // 요청(Request)과 연결된 세부 인증 정보 설정
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContext 생성 및 인증 정보 설정
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authenticationToken);

            // 외부에서 사용 가능하도록 컨텍스트 설정
            SecurityContextHolder.setContext(securityContext);

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // 요청&응답을 다음 filter 로 넘기기
        filterChain.doFilter(request, response);
    }
}