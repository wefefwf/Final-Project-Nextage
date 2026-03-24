package com.nextage.web.interceptor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleRedirectInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() 
            || authentication.getPrincipal().equals("anonymousUser")) {
            return true;
        }

        String requestURI = request.getRequestURI();
        boolean isBusiness = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().startsWith("ROLE_B"));

        // 기업 유저가 고객 페이지 직접 접근
        if (isBusiness && requestURI.startsWith("/customer")
                && !requestURI.startsWith("/customer/request")) {
        	response.sendRedirect("/business/main?accessDenied=true");
            return false;
        }

        // 고객 유저가 기업 페이지 직접 접근
        if (!isBusiness && requestURI.startsWith("/business")
                && !requestURI.startsWith("/business/portfolio")) {
        	response.sendRedirect("/customer/main?accessDenied=true");
            return false;
        }

        return true;
    }
}
