package com.nextage.web.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nextage.web.domain.BidDTO;
import com.nextage.web.service.BidService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/business/bids")
public class BusinessBidsController {

	private final BidService bidService;

	 // 비즈니스 마이페이지: 제안(bid) 목록
	@GetMapping("/mypage")
	public String myBidsPage(Model model, Authentication authentication) {
		// 로그인 정보에서 businessId 추출
		Long businessId = Long.valueOf(authentication.getName());

		List<BidDTO> myBids = bidService.getBidsByBusinessId(businessId);
		model.addAttribute("bids", myBids);
		return "business/mypage";
	}

}
