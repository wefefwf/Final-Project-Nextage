package com.nextage.web.config;

import com.nextage.web.service.BusinessUserDetailsService;
import com.nextage.web.service.CustomerUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomerUserDetailsService customerUserDetailsService;
    private final BusinessUserDetailsService businessUserDetailsService;

    // @Bean
    // public BCryptPasswordEncoder passwordEncoder() {
    //     return new BCryptPasswordEncoder();
    // }

    @Bean
public PasswordEncoder passwordEncoder() {
    return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
}

    @Bean
public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers(
        "/css/**", 
        "/js/**", 
        "/image/**",     
        "/img/**",        
        "/bootstrap/**",  
        "/images/**",     
        "/favicon.ico", 
        "/error"          
    );
}

    // 기업 보안 설정
    @Bean
    @Order(1)
    public SecurityFilterChain businessFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/business/**", "/auth/business/**") // 기업 경로만 담당
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/business/login", "/auth/business/loginProc","/business/main","/business/request","/business/request/list","/business/portfolio/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/business/login")
                .loginProcessingUrl("/auth/business/loginProc")
                .usernameParameter("loginId")
                .passwordParameter("password")
                .defaultSuccessUrl("/business/main", true)
                .permitAll()
            )
            .userDetailsService(businessUserDetailsService)
            .logout(logout -> logout
                .logoutUrl("/auth/business/logoutAction")
                .logoutSuccessUrl("/business/main")
            );
        return http.build();
    }

    //  고객 보안 설정
    @Bean
    @Order(2)
    public SecurityFilterChain customerFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/customer/login", "/auth/customer/loginProc", "/auth/join","/customer/main","/customer/shop","/customer/shop/detail","/customer/request/detail/**","/customer/request","/customer/cart","/customer/request/list","/api/bids/request/**").permitAll()//로그인안해도 접속가능한링크
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/customer/login")
                .loginProcessingUrl("/auth/loginProc")
                .usernameParameter("loginId")
                .passwordParameter("password")
                .defaultSuccessUrl("/customer/main", true)
                .permitAll()
            )
            .userDetailsService(customerUserDetailsService) 
            .logout(logout -> logout
                .logoutUrl("/auth/customer/logoutAction")
                .logoutSuccessUrl("/customer/main")
            );
        return http.build();
    }
}