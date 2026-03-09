package com.nextage.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nextage.web.mapper.CustomerShopMapper;

@Service
public class CustomerShopService {

	@Autowired
	public CustomerShopMapper shopMapper;
	
}
