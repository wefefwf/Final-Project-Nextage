package com.nextage.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.nextage.web.domain.RequestDTO;
import com.nextage.web.service.CustomerRequestService;

@Controller
public class CustomerMainController {

    @Autowired
    private CustomerRequestService requestService;

    @GetMapping("/customer/main")
    public String main(Model model) {
        // 1. 기존 트렌드 데이터
        model.addAttribute("urgentRequests", requestService.getUrgentRequests());
        model.addAttribute("newRequests", requestService.getNewRequests());
        model.addAttribute("bestReviews", requestService.getBestReviews());
        
        // 2. 내 의뢰 목록 데이터 추가 (현재는 테스트를 위해 1L번 고객으로 고정)
        // 나중에 세션 도입 후: Long customerId = (Long) session.getAttribute("customerId");
        Long customerId = 1L; 
        List<RequestDTO> myRequests = requestService.getRequestsByCustomerId(customerId);
        
        // 타임리프 에러 방지용 null 체크: 데이터가 없으면 빈 리스트를 보냅니다.
        model.addAttribute("myRequests", myRequests != null ? myRequests : new ArrayList<>());
        
        return "views/main/customer-main"; 
    }
}