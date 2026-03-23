package com.nextage.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.nextage.web.domain.NoticeDTO;
import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.domain.ReviewDTO;
import com.nextage.web.domain.ScheduleOrderDTO;
import com.nextage.web.service.BusinessOrderHistoryService;
import com.nextage.web.service.BusinessPortfolioService;
import com.nextage.web.service.CustomerRequestService;
import com.nextage.web.service.NoticeService;
import com.nextage.web.userDetails.BusinessUserDetails;

@Controller
public class BusinessMainController {

    @Autowired
    private CustomerRequestService requestService;

    @Autowired
    private BusinessPortfolioService pfService;

    @Autowired
    private BusinessOrderHistoryService businessOrderHistoryService;
    
    @Autowired
    private NoticeService noticeService;

    @GetMapping("/business/main")
    public String main(@AuthenticationPrincipal BusinessUserDetails businessUserDetails, Model model) {

        Long businessId = null;
        String companyName = "";
        String role = "BUSER"; 

        if (businessUserDetails != null) {
            businessId  = businessUserDetails.getBusiness().getBusinessId();
            companyName = businessUserDetails.getBusiness().getCompanyName();
            role        = businessUserDetails.getRole(); 
        }

        // 1. 의뢰글 리스트
        List<RequestDTO> newPostList = requestService.getAllRequests();
        if (newPostList.size() > 5) {
            newPostList = newPostList.subList(0, 5);
        }

        // 2. 포폴 리스트
        List<ReviewDTO> portfolioList = null;
        if (businessId != null) {
            portfolioList = pfService.getReview(businessId, 8, 0, true);
        }

        // 3. 새로 들어온 주문 (최대 5건)
        List<OrderHistoryDTO> pendingOrders = List.of();
        if (businessId != null) {
            pendingOrders = businessOrderHistoryService.getPendingOrders(businessId, role); // ← 소문자
            if (pendingOrders.size() > 5) {
                pendingOrders = pendingOrders.subList(0, 5);
            }
        }

        //4. 캘린더
     // 4. 전체 일정 및 오늘 작업
        List<ScheduleOrderDTO> scheduleList = businessOrderHistoryService.getScheduleOrders(businessId);
        scheduleList.sort((a, b) -> a.getDueDate().compareTo(b.getDueDate()));

        LocalDate today = LocalDate.now();
        List<ScheduleOrderDTO> todayWork = scheduleList.stream()
                .filter(order -> !order.getCreatedAt().toLocalDate().isAfter(today)
                              && !order.getDueDate().toLocalDate().isBefore(today))
                .collect(Collectors.toList());

        
        
        List<NoticeDTO> notices = noticeService.getFindBusinessNotices();
        model.addAttribute("businessNotices", notices);
        
        model.addAttribute("scheduleList", scheduleList);
        model.addAttribute("todayWork", todayWork);
        model.addAttribute("businessId",    businessId);
        model.addAttribute("companyName",   companyName);
        model.addAttribute("newPostList",   newPostList);
        model.addAttribute("portfolioList", portfolioList);
        model.addAttribute("pendingOrders", pendingOrders);

        return "views/main/business-main";
    }
}