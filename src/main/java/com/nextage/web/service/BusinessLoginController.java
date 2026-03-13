package com.nextage.web.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.nextage.web.domain.BusinessDTO;
import com.nextage.web.userDetails.BusinessUserDetails;

@Controller
public class BusinessLoginController {
	
	@GetMapping("/business/login")
    public String businessLogin() {
        return "views/login/business-login";
    }
 
    @PreAuthorize("hasAnyRole('BUSER','BADMIN')") 
    @GetMapping("/business/mypage")
    public String mypage(@AuthenticationPrincipal BusinessUserDetails businessUserDetails, Model model) {
    	 BusinessDTO  business = businessUserDetails.getBusiness();
    	 model.addAttribute("business", business);
        return "views/login/business-mypage";
    }
  
	
}
