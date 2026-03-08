package com.nextage.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
	    registry.addResourceHandler("/images/**")
	            // file:/// 다음에 D:/ 경로를 정확히 붙여주세요
	            .addResourceLocations("file:///D:/nextageImage/"); 
	}
}