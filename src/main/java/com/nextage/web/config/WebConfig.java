package com.nextage.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.nextage.web.interceptor.RoleRedirectInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RoleRedirectInterceptor roleRedirectInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///D:/nextageImage/");
    }

    // 👇 이것만 추가!
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    
   
    registry.addInterceptor(roleRedirectInterceptor)
    .addPathPatterns("/**")
    .excludePathPatterns("/css/**", "/js/**", "/image/**", "/images/**", "/favicon.ico");
     }
}