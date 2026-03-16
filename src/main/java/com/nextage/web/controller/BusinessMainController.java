package com.nextage.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.domain.ReviewDTO;
import com.nextage.web.service.BusinessOrderHistoryService;
import com.nextage.web.service.BusinessPortfolioService;
import com.nextage.web.service.CustomerRequestService;
import com.nextage.web.userDetails.BusinessUserDetails;

@Controller
public class BusinessMainController {

    @Autowired
    private CustomerRequestService requestService;

    @Autowired
    private BusinessPortfolioService pfService;

    @Autowired
    private BusinessOrderHistoryService businessOrderHistoryService;

    @GetMapping("/business/main")
    public String main(@AuthenticationPrincipal BusinessUserDetails businessUserDetails, Model model) {

        Long businessId = null;
        String companyName = "";
        String role = "BUSER"; // 기본값

        if (businessUserDetails != null) {
            businessId  = businessUserDetails.getBusiness().getBusinessId();
            companyName = businessUserDetails.getBusiness().getCompanyName();
            role        = businessUserDetails.getRole(); // ← 추가
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
            if (pendingOrders.size() > 1) {
                pendingOrders = pendingOrders.subList(0, 1);
            }
        }

        model.addAttribute("businessId",    businessId);
        model.addAttribute("companyName",   companyName);
        model.addAttribute("newPostList",   newPostList);
        model.addAttribute("portfolioList", portfolioList);
        model.addAttribute("pendingOrders", pendingOrders);

        return "views/main/business-main";
    }
}