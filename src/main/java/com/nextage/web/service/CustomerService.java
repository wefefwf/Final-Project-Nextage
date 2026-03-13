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

//    public void register(CustomerDTO customer) {
//        customer.setPasswordHash(passwordEncoder.encode(customer.getPasswordHash()));
//        customerMapper.insertCustomer(customer);
//    }
//
//    public void modify(CustomerDTO customer) {
//        customerMapper.updateCustomer(customer);
//    }

}
