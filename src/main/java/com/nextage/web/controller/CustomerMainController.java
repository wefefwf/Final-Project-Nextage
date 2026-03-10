package com.nextage.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nextage.web.service.CustomerRequestService;

@Controller
public class CustomerMainController {

    @Autowired
    private CustomerRequestService requestService; // 서비스 주입
    
    // 고객 메인 페이지
    @GetMapping("/customer/main")
    public String main(Model model) {
        model.addAttribute("urgentRequests", requestService.getUrgentRequests());
        model.addAttribute("newRequests", requestService.getNewRequests());
        model.addAttribute("bestReviews", requestService.getBestReviews());
        
        return "views/main/customer-main"; 
    }
}