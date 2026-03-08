package com.nextage.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 1. CSRF 보안 해제 (개발 시 필수)
            .authorizeHttpRequests(auth -> auth
                // 2. 모든 요청(/**)을 무조건 허용! (로그인 창 안 뜨게 함)
                .requestMatchers("/**").permitAll() 
                .anyRequest().permitAll()
            )
            // 3. 시큐리티 기본 로그인 폼 기능을 아예 꺼버림
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}