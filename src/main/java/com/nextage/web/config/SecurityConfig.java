package com.nextage.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


//자꾸 로그인으로 가서 개발을 위해 잠시 시큐리티를 풀어두니 양해바랍니다.

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		http.authorizeHttpRequests(auth -> auth
		        .requestMatchers("/customer","/business").permitAll().requestMatchers("/**").permitAll());
		
		http.csrf(csrf -> csrf.disable());
		
// 로그아웃 시 이동 담당자 설정		
//		http.logout(logout -> logout
//				.logoutSuccessUrl(로그아웃 시 이동될 구역)
//				.invalidateHttpSession(true));
//		
		return http.build();
	}
	
}
