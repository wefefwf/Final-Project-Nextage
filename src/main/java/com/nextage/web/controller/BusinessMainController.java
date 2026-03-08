package com.nextage.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/business")
public class BusinessMainController {
	
	//비즈니스 메인 페이지
	@GetMapping("/main")
    public String main() {
        return "views/main/business-main";
    }
}
