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
		public String goPortfolio(@AuthenticationPrincipal BusinessUserDetails businessUserDetails,Model model,
				@RequestParam(value="page", defaultValue = "0") int page){
			
		 	int size = 6;
		    int offset = page * size;
		    int blockLimit = 10; // 한 번에 보여줄 페이지 번호 개수
		    
			//시큐리티에서 값꺼내오기 
			BusinessDTO  business = businessUserDetails.getBusiness();
			
			Boolean isMine = true;
			
			BizinfoDTO bizinfo = portfolioService.getPortfolio(business.getBusinessId());
			List<ReviewDTO> reviewList = portfolioService.getReview(business.getBusinessId(),size,offset,isMine);
			List<CareerDTO> careerList = portfolioService.getCareer(business.getBusinessId());
			
			// 2. 전체 개수 및 페이지네이션 계산 (추가된 부분)
	        long totalReviews = portfolioService.getTotalReviewCount(business.getBusinessId(),isMine); 
	        int totalPages = (int) Math.ceil((double) totalReviews / size);
	        
	     // 시작 페이지 계산 (0, 10, 20...)
	        int startPage = (((int)(Math.ceil((double)(page + 1) / blockLimit))) - 1) * blockLimit;
	        // 끝 페이지 계산 (전체 페이지 수를 넘지 않게)
	        int endPage = Math.min(startPage + blockLimit - 1, totalPages - 1);
	        if (endPage < 0) endPage = 0;
	        
			model.addAttribute("bizinfo",bizinfo);
			model.addAttribute("reviewList",reviewList);
			model.addAttribute("careerList",careerList);
			//비즈니스인지 판별
			model.addAttribute("isMine", true);		
			
			//페이지네이션
			model.addAttribute("size", size);						
			model.addAttribute("offset", offset);
			model.addAttribute("page", page);						
			// 페이지네이션 관련 모델 추가
	        model.addAttribute("page", page);						
	        model.addAttribute("startPage", startPage);
	        model.addAttribute("endPage", endPage);
	        model.addAttribute("totalPages", totalPages);
	        
				return "views/portfolio/portfolio";
			}
	
	
	//portfolio페이지 가기 -customer
	@GetMapping("/business/portfolio/{businessId}")
		public String viewPortfolio(@PathVariable("businessId") Long businessId, Model model,
				@RequestParam(value="page", defaultValue = "0") int page) {
		
		 int size = 6;
		 int offset = page * size;
		 int blockLimit = 10; // 한 번에 보여줄 페이지 번호 개수
		 
		 Boolean isMine = false;
		 
		BizinfoDTO bizinfo = portfolioService.getPortfolio(businessId);
		List<ReviewDTO> reviewList = portfolioService.getReview(businessId,size,offset,isMine);
		List<CareerDTO> careerList = portfolioService.getCareer(businessId);
		
		// 2. 전체 개수 및 페이지네이션 계산 (추가된 부분)
        long totalReviews = portfolioService.getTotalReviewCount(businessId,isMine); 
        int totalPages = (int) Math.ceil((double) totalReviews / size);
        
        // 시작 페이지 계산 (0, 10, 20...)
        int startPage = (((int)(Math.ceil((double)(page + 1) / blockLimit))) - 1) * blockLimit;
        // 끝 페이지 계산 (전체 페이지 수를 넘지 않게)
        int endPage = Math.min(startPage + blockLimit - 1, totalPages - 1);
        if (endPage < 0) endPage = 0;
        
		model.addAttribute("bizinfo",bizinfo);
		model.addAttribute("reviewList",reviewList);
		model.addAttribute("careerList",careerList);
		//비즈니스인지 판별
		model.addAttribute("isMine", false);	
		
		//페이지네이션
		model.addAttribute("size", size);						
		model.addAttribute("offset", offset);
		// 페이지네이션 관련 모델 추가
        model.addAttribute("page", page);						
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);
		
		return "views/portfolio/portfolio";
	}
	
	
}
