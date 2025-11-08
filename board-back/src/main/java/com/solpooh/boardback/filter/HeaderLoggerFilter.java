package com.solpooh.boardback.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Enumeration;

@Slf4j
@Component
public class HeaderLoggerFilter implements Filter {
    private final String EXCLUDE_PATH = "/actuator/prometheus";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 요청/응답 Body를 여러 번 읽을 수 있게 캐싱
        // Servlet의 InputStream은 한 번 읽으면 다시 읽을 수 없기 때문에,
        // Filter 단계에서 Body를 읽어버리면 Controller에서는 더 이상 데이터를 읽을 수 없게 된
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper((HttpServletResponse) response);

        chain.doFilter(req, res);

        Enumeration<String> headerNames = req.getHeaderNames();
        StringBuilder headerValues = new StringBuilder();

        headerNames.asIterator().forEachRemaining(headerKey -> {
            String headerValue = req.getHeader(headerKey);

            headerValues.append("[")
                    .append(headerKey)
                    .append(" : ")
                    .append(headerValue).append("]");
        });

        String requestBody = new String(req.getContentAsByteArray());
        String uri = req.getRequestURI();
        String method = req.getMethod();

        if (!uri.contains(EXCLUDE_PATH))
            log.info("Request: [{} {}] - Headers: {} - Body: {}", method, uri, headerValues, requestBody);

        res.copyBodyToResponse();
    }
}
