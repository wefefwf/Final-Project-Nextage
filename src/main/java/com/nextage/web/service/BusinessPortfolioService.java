package com.nextage.web.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.nextage.web.domain.KitDTO;
import com.nextage.web.mapper.BusinessPortfolioMapper;
import com.nextage.web.mapper.CustomerShopMapper;

@Service
public class BusinessPortfolioService {

	@Autowired
	public BusinessPortfolioMapper portfolioMapper;
	
}
