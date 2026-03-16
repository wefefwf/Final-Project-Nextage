package com.nextage.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.domain.ReviewDTO;
import com.nextage.web.service.BusinessPortfolioService;
import com.nextage.web.service.CustomerRequestService;
import com.nextage.web.userDetails.BusinessUserDetails;

@Controller
public class BusinessMainController {

    @Autowired
    private CustomerRequestService requestService;

    @Autowired
    private BusinessPortfolioService pfService;
    
    //메인 가기 
    @GetMapping("/business/main")
    public String main(@AuthenticationPrincipal BusinessUserDetails businessUserDetails,Model model) {
    	
    	// 1. 시큐리티에서 로그인한 사장님 정보 꺼내오기
        Long businessId = null;
        if (businessUserDetails != null) {
            businessId = businessUserDetails.getBusiness().getBusinessId();
        }
    	
    	//1. 의뢰글 리스트
        List<RequestDTO> newPostList = requestService.getAllRequests();
        // 최신 5개만 잘라서 넘기기
        if (newPostList.size() > 5) {
            newPostList = newPostList.subList(0, 5);
        }
        
        //2. 포폴 review리스트
        List<ReviewDTO> portfolioList = null;
        if (businessId != null) {
            // id: 본인ID, size: 8개(슬라이더용), offset: 0, isMine: true
            portfolioList = pfService.getReview(businessId, 8, 0, true);
        }
        
        model.addAttribute("businessId", businessId);
        model.addAttribute("newPostList", newPostList);
        model.addAttribute("portfolioList", portfolioList);
        return "views/main/business-main";
    }
}
