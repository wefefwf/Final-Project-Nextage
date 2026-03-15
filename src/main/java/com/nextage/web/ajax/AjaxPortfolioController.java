package com.nextage.web.ajax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nextage.web.service.BusinessPortfolioService;
import com.nextage.web.userDetails.BusinessUserDetails;

@RestController
public class AjaxPortfolioController {

	@Autowired
	public BusinessPortfolioService pService;
	
	//포폴 카드 상태 업로드
	@PostMapping("/business/portfolio/updateStatus")
	public void updateStatus( @RequestParam("reviewId") long reviewId,@RequestParam("status") String status) {
	    pService.updateStatus(reviewId, status);
	}
	
	//위치 추가,수정
	@PostMapping("/business/portfolio/updateLocation")
	public String updateLocation( @AuthenticationPrincipal BusinessUserDetails userDetails, @RequestParam("location") String location){
		
		Long businessId = userDetails.getBusiness().getBusinessId();
		
		pService.updateLocation(businessId,location);
		
		return "success";
	}
	
	//위치 삭제
	@PostMapping("/business/portfolio/delete")
	public String deleteItem(@AuthenticationPrincipal BusinessUserDetails userDetails,
	                         @RequestParam("type") String type,
	                         @RequestParam(value = "id", required = false) Long id) {

	    Long businessId = userDetails.getBusiness().getBusinessId();

	    try {

	        if ("location".equals(type)) {
	            pService.deleteLocation(businessId);
	        } 
	        else if ("profile".equals(type)) {
	            pService.deleteProfileImage(businessId);
	        } 
	        else if ("career".equals(type)) {
	            pService.deleteCareer(id);
	        }

	        return "success";

	    } catch (Exception e) {
	        return "error";
	    }
	}
	
	//경력 수정
	@PostMapping("/business/portfolio/updateCareer")
	public void updateCareer(@RequestParam("careerId")long careerId,@RequestParam("workDescription") String workDescription){
		
		//careerId = 글번호, workDescription = 경력내용변경
		pService.updateCareer(careerId,workDescription);
	}
	
	//경력 추가
	@PostMapping("/business/portfolio/addCareer")
	public void addCareer(@AuthenticationPrincipal BusinessUserDetails userDetails,@RequestParam("workDescription") String workDescription){
		Long businessId = userDetails.getBusiness().getBusinessId();
		pService.addCareer(businessId, workDescription);
		
	}
	
}
