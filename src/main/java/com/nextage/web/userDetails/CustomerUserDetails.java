package com.nextage.web.userDetails;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.nextage.web.domain.CustomerDTO;

import lombok.Getter;

@Getter
public class CustomerUserDetails implements UserDetails {
	
	private final CustomerDTO customer;

    public CustomerUserDetails(CustomerDTO customer) {
        this.customer = customer;
    }

    public Long getCustomerId() { return customer.getCustomerId(); }
    public String getRole()     { return customer.getRole(); }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + customer.getRole()));
    }

    @Override
    public String getPassword() {
        return customer.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return customer.getLoginId();
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
        return "ACTIVE".equals(customer.getStatus());
    }

}
