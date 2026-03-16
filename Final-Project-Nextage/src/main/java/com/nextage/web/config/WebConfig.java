package com.nextage.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 브라우저에서 /images/** 요청하면 D:/nextageImage/ 폴더를 보여줌
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///D:/nextageImage/");
        
        // static 폴더는 Spring Boot가 자동 처리하므로 따로 등록하지 않음
    }
}