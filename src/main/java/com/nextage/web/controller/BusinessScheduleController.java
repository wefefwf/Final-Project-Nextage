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
import com.nextage.web.service.BusinessOrderHistoryService;
import com.nextage.web.service.BusinessPortfolioService;
import com.nextage.web.service.CustomerShopService;
import com.nextage.web.userDetails.BusinessUserDetails;

import jakarta.servlet.http.HttpSession;

@Controller
public class BusinessScheduleController {

	@Autowired
	public BusinessOrderHistoryService bohService;

	/*
	 * //portfolio페이지 가기 -business
	 * 
	 * @PreAuthorize("hasAnyRole('BUSER','BADMIN')")
	 * 
	 * @GetMapping("/business/schedule") public String
	 * goSchedule(@AuthenticationPrincipal BusinessUserDetails user,Model model){
	 * 
	 * 
	 * return ss; }
	 */
}
