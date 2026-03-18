package com.nextage.web.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nextage.web.domain.BusinessDTO;
import com.nextage.web.mapper.BusinessMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessService {
	
	private final BusinessMapper businessMapper;
    private final PasswordEncoder passwordEncoder;

    public void register(BusinessDTO business) {
        business.setPasswordHash(passwordEncoder.encode(business.getPasswordHash()));
        business.setRole("BUSER");
        business.setStatus("ACTIVE");
        businessMapper.insertBusiness(business);
    }
    public boolean isIdDuplicate(String loginId) {
        return businessMapper.countByLoginId(loginId) > 0 ;
    }
    public boolean isNameDuplicate(String companyName) {
        return businessMapper.countByCompanyName(companyName) > 0 ;
    }
    public boolean isPhoneDuplicate(String phoneNumber) {
    	System.out.println("입력값: [" + phoneNumber + "]");

        int count = businessMapper.countByPhoneNumber(phoneNumber);

        System.out.println("조회결과 count: " + count);
    	
        return businessMapper.countByPhoneNumber(phoneNumber) > 0 ;
    }
}
