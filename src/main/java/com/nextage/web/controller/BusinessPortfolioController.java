package com.nextage.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.nextage.web.domain.KitDTO;
import com.nextage.web.service.BusinessPortfolioService;
import com.nextage.web.service.CustomerShopService;

import jakarta.servlet.http.HttpSession;

@Controller
public class BusinessPortfolioController {

	@Autowired
	public BusinessPortfolioService portfolioService;

	//portfolio페이지 가기
	//business는 세션에서 id값빼고 user은 글에서 가져옴
	//@GetMapping("/business/portfolio/{id}")
	@GetMapping("/business/my-portfolio")
			public String goPortfolio(){
				return "views/portfolio/portfolio";
			}
}
