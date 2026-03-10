package com.nextage.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nextage.web.domain.KitDTO;
import com.nextage.web.mapper.CustomerShopMapper;

@Service
public class CustomerShopService {

	@Autowired
	public CustomerShopMapper shopMapper;
	
	public List<KitDTO> getKitListPaged(int offset, int size,String sort) {
	    return shopMapper.getKitListPaged(offset, size,sort);
	}

	public int getTotalKitCount() {
	    return shopMapper.getTotalKitCount();
	}
	
	
	
}
