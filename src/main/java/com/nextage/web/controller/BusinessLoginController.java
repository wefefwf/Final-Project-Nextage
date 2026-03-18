package com.nextage.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nextage.web.domain.BusinessDTO;
import com.nextage.web.service.BusinessService;
import com.nextage.web.userDetails.BusinessUserDetails;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BusinessLoginController {

    private final BusinessService businessService;


	@GetMapping("/business/login")
    public String businessLogin() {
        return "views/login/business-login";
    }
	
	@GetMapping("/business/join")
    public String businessJoin() {
        return "views/login/business-join";
    }
 
    @PreAuthorize("hasRole('BUSER')") 
    @GetMapping("/business/mypage")
    public String mypage(@AuthenticationPrincipal BusinessUserDetails businessUserDetails, Model model) {
    	 BusinessDTO  business = businessUserDetails.getBusiness();
    	 model.addAttribute("business", business);
        return "views/login/business-mypage";
    }
    
    @PostMapping("/auth/business/registerProc")
    public String joinProc(BusinessDTO dto) {
    	businessService.register(dto);
        return "redirect:/business/login?success"; 
    }
    
    @GetMapping("/auth/business/check")
    @ResponseBody
    public boolean checkId(@RequestParam("loginId") String loginId) {
        return businessService.isIdDuplicate(loginId);
    }
    
    @GetMapping("/auth/business/check/phone")
    @ResponseBody
    public boolean checkPhone(@RequestParam("phoneNumber") String phoneNumber) {
        return businessService.isPhoneDuplicate(phoneNumber);
    }
    
    @GetMapping("/auth/business/check/name")
    @ResponseBody
    public boolean checkName(@RequestParam("companyName") String companyName) {
        return businessService.isNameDuplicate(companyName);
    }
    

  
	
}
