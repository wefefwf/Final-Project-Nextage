package com.nextage.web.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nextage.web.domain.CustomerDTO;
import com.nextage.web.mapper.CustomerMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {
	private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    public void register(CustomerDTO customer) {
        customer.setPasswordHash(passwordEncoder.encode(customer.getPasswordHash()));
        customer.setRole("CUSER");
        customer.setStatus("ACTIVE");
        customerMapper.insertCustomer(customer);
    }
    
    public boolean isIdDuplicate(String loginId) {
        return customerMapper.countByLoginId(loginId) > 0 ;
    }
    
    public boolean isNameDuplicate(String nickname) {
        return customerMapper.countByNickname(nickname) > 0 ;
    }
    
    public boolean isPhoneDuplicate(String phoneNumber) {
        return customerMapper.countByPhone(phoneNumber) > 0 ;
    }
    
    public boolean isEmailDuplicate(String email) {
    	email = email.toLowerCase().trim();
        return customerMapper.countByEmail(email) > 0 ;
    }
    
    
    public CustomerDTO getCustomerByLoginId(String loginId) {
        return customerMapper.findByLoginId(loginId);
    }

    public void updateCustomer(CustomerDTO dto) {
 
        if (dto.getNewPassword() != null && !dto.getNewPassword().trim().isEmpty()) {
            dto.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        }
        
        customerMapper.updateCustomer(dto);
    }

    public void withdraw(String loginId) {
        customerMapper.updateRoleToNull(loginId);
    }
    
    
    
}
