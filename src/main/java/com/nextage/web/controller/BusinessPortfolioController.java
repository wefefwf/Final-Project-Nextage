package com.nextage.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.nextage.web.domain.BizinfoDTO;
import com.nextage.web.domain.BusinessDTO;
import com.nextage.web.domain.CareerDTO;
import com.nextage.web.domain.KitDTO;
import com.nextage.web.domain.ReviewDTO;
import com.nextage.web.service.BusinessPortfolioService;
import com.nextage.web.service.CustomerShopService;
import com.nextage.web.userDetails.BusinessUserDetails;

import jakarta.servlet.http.HttpSession;

@Controller
public class BusinessPortfolioController {

	@Autowired
	public BusinessPortfolioService portfolioService;

	//portfolio페이지 가기 -business
	@PreAuthorize("hasAnyRole('BUSER','BADMIN')") 
	@GetMapping("/business/my-portfolio")
		public String goPortfolio(@AuthenticationPrincipal BusinessUserDetails businessUserDetails,Model model){
			
			//시큐리티에서 값꺼내오기 
			BusinessDTO  business = businessUserDetails.getBusiness();
			
			BizinfoDTO bizinfo = portfolioService.getPortfolio(business.getBusinessId());
			List<ReviewDTO> reviewList = portfolioService.getReview(business.getBusinessId());
			List<CareerDTO> careerList = portfolioService.getCareer(business.getBusinessId());
			
			model.addAttribute("bizinfo",bizinfo);
			model.addAttribute("reviewList",reviewList);
			model.addAttribute("careerList",careerList);
			//비즈니스인지 판별
			model.addAttribute("isMine", true);						
				return "views/portfolio/portfolio";
			}
	
	
	//portfolio페이지 가기 -customer
	@GetMapping("/business/portfolio/{businessId}")
		public String viewPortfolio(@PathVariable("businessId") Long businessId, Model model) {
		
		BizinfoDTO bizinfo = portfolioService.getPortfolio(businessId);
		List<ReviewDTO> reviewList = portfolioService.getReview(businessId);
		List<CareerDTO> careerList = portfolioService.getCareer(businessId);
		
		model.addAttribute("bizinfo",bizinfo);
		model.addAttribute("reviewList",reviewList);
		model.addAttribute("careerList",careerList);
		//비즈니스인지 판별
		model.addAttribute("isMine", false);				
		return "views/portfolio/portfolio";
	}
	
	
	
}
