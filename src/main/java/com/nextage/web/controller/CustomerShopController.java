package com.nextage.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerShopController {

	 // 샵 가기 페이지
    @GetMapping("/customer/shop")
    public String main() {
        return "views/shop/customer-shop";
    }
}
