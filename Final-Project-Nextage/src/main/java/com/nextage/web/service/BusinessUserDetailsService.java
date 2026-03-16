package com.nextage.web.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nextage.web.domain.BusinessDTO;
import com.nextage.web.mapper.BusinessMapper;
import com.nextage.web.userDetails.BusinessUserDetails;

@Service
@RequiredArgsConstructor
public class BusinessUserDetailsService implements UserDetailsService {
	
    private final BusinessMapper businessMapper;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        BusinessDTO business = businessMapper.findByLoginId(loginId);
        if (business == null) throw new UsernameNotFoundException("기업 없음");

        return new BusinessUserDetails(business);
    }
}