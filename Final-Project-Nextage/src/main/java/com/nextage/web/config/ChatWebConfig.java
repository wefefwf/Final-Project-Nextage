package com.nextage.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ChatWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = "file:///" + System.getProperty("user.dir") + "/src/main/resources/static/images/chat/";
        
        registry.addResourceHandler("/images/chat/**")
                .addResourceLocations(uploadPath);
    }
}