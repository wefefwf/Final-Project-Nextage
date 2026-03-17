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
import org.springframework.web.bind.annotation.ModelAttribute;
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
import com.nextage.web.userDetails.CustomerUserDetails;

import jakarta.servlet.http.HttpSession;

@Controller
public class CustomerReviewController {

	@Autowired
	public CustomerReviewService cService;

	 //review가기 
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

	 //review 추가하기
	 @PostMapping("/customer/review/save")
	 	public String insertReview(
	 			@RequestParam("orderItemId") Long orderItemId,
	 		    @RequestParam("businessId") Long businessId,
	 		    @RequestParam("orderId") Long orderId,
	 		    @RequestParam("content") String content,
	 		    @RequestParam("image1") MultipartFile image1,
	 		    @RequestParam(value = "image2", required = false) MultipartFile image2,
	 		    @RequestParam(value = "image3", required = false) MultipartFile image3,
	 		    @AuthenticationPrincipal CustomerUserDetails customerUserDetails) {
		 
	     ReviewDTO reviewDTO = new ReviewDTO();
	     reviewDTO.setOrderItemId(orderItemId);
	     reviewDTO.setBusinessId(businessId);
	     reviewDTO.setContent(content);
	     reviewDTO.setCustomerId(customerUserDetails.getCustomerId());
	     
	     cService.insertReview(reviewDTO, image1, image2, image3);
	     return "redirect:/customer/order/detail/" + orderId;
	 }
}