package com.nextage.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.nextage.web.domain.BizinfoDTO;
import com.nextage.web.domain.BusinessDTO;
import com.nextage.web.domain.CareerDTO;
import com.nextage.web.domain.KitDTO;
import com.nextage.web.domain.ReviewDTO;
import com.nextage.web.domain.ScheduleOrderDTO;
import com.nextage.web.service.BusinessOrderHistoryService;
import com.nextage.web.service.BusinessPortfolioService;
import com.nextage.web.service.CustomerReviewService;
import com.nextage.web.service.CustomerShopService;
import com.nextage.web.userDetails.BusinessUserDetails;

import jakarta.servlet.http.HttpSession;

@Controller
public class CustomerReviewController {

	@Autowired
	public CustomerReviewService cService;

	
	  //portfolio페이지 가기 -business
	  
	 @PreAuthorize("hasAnyRole('CUSER','CADMIN')") 
	 @GetMapping("/customer/review/write")
	 public String goReview(@AuthenticationPrincipal UserDetails userDetails,Model model,@RequestParam("orderItemId") long orderItemId ){

		 //유저 이름 
		 String loginId = userDetails.getUsername();
		 //주문 내역
		 ReviewDTO orderInfo = cService.getOrderItemForReview(orderItemId,loginId);
		 
		 if (orderInfo == null) {
		        // 본인 주문이 아니거나 데이터가 없으면 리스트로 튕겨내기
		        return "redirect:/customer/order/history";
		    }
		 
		 model.addAttribute("orderInfo", orderInfo);
		 return "views/review/customer-review";
	 } 

}
