package com.nextage.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.nextage.web.service.CustomerShopService;

@Controller
public class CustomerShopController {

	@Autowired
	public CustomerShopService shopService;
	
	 // 샵 가기 페이지
	//리스트 뽑아가기
    @GetMapping("/customer/shop")
    public String getList(Model model) {
    	
    	
        return "views/shop/customer-shop";
    }
    
    
    
    
    
    
    
    
    
    
}
