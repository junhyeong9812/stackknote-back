package com.stacknote.back.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 * 정적 리소스 핸들링, 파일 업로드 경로 설정 등
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:images/}")
    private String uploadPath;

    /**
     * 정적 리소스 핸들러 설정
     * 업로드된 파일들을 정적 리소스로 서빙
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 이미지 파일 서빙
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath);

        // Swagger UI 리소스 핸들링
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");

        // 기본 정적 리소스 핸들링
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}