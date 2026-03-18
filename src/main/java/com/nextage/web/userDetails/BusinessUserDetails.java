package com.nextage.web.userDetails;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.nextage.web.domain.BusinessDTO;

import lombok.Getter;

@Getter
public class BusinessUserDetails implements UserDetails{
	
	private final BusinessDTO business;

    public BusinessUserDetails(BusinessDTO business) {
        this.business = business;
    }

    public Long getBusinessId() { return business.getBusinessId(); }
    public String getRole()     { return business.getRole(); }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + business.getRole()));
    }

    @Override
    public String getPassword() {
        return business.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return business.getLoginId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(business.getStatus());
    }
}
