package com.nextage.web.ajax;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nextage.web.service.BusinessPortfolioService;

@RestController
public class AjaxPortfolioController {

	@Autowired
	public BusinessPortfolioService pService;
	
	@PostMapping("/business/portfolio/updateStatus")
	public void updateStatus( @RequestParam("reviewId") long reviewId,@RequestParam("status") String status) {

	    pService.updateStatus(reviewId, status);

	}
	
}
