package com.nextage.web.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nextage.web.UserDetails.CustomerUserDetails;
import com.nextage.web.domain.CustomerDTO;
import com.nextage.web.mapper.CustomerMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {
	
    private final CustomerMapper customerMapper;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        CustomerDTO customer = customerMapper.findByLoginId(loginId);
        if (customer == null) throw new UsernameNotFoundException("없는아이디입니다.");

        return new CustomerUserDetails(customer);
    }
}
