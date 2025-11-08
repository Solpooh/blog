package com.solpooh.boardback.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedMethods("*")
                .allowedOrigins("*");
    }

//    @Bean
//    @Deprecated
//    public FilterRegistrationBean<ApiRequestLoggingFilter> apiLoggingFilter() {
//        FilterRegistrationBean<ApiRequestLoggingFilter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(new ApiRequestLoggingFilter());
//        registration.addUrlPatterns("/*");
//        registration.setOrder(1);
//
//        return registration;
//    }
}
