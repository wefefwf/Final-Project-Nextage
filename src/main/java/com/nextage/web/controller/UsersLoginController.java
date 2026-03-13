package com.nextage.web.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.nextage.web.UserDetails.BusinessUserDetails;
import com.nextage.web.UserDetails.CustomerUserDetails;
import com.nextage.web.domain.BusinessDTO;
import com.nextage.web.domain.CustomerDTO;


@Controller
public class UsersLoginController {

    @GetMapping("/login")
    public String customerLogin() {
        return "views/customer/login";
    }


    @GetMapping("/business/login")
    public String businessLogin() {
        return "views/business/login";
    }
 
	    @PreAuthorize("hasRole('CUSER')")
	    @GetMapping("/customer/mypage")
    public String mypage(@AuthenticationPrincipal CustomerUserDetails customerUserDetails, Model model) {
        CustomerDTO customer = customerUserDetails.getCustomer();
        model.addAttribute("customer", customer);
        return "views/customer/mypage";
    }
    
    @PreAuthorize("hasAnyRole('BUSER','BADMIN')") 
    @GetMapping("/business/mypage")
    public String mypage(@AuthenticationPrincipal BusinessUserDetails businessUserDetails, Model model) {
    	 BusinessDTO  business = businessUserDetails.getBusiness();
    	 model.addAttribute("business", business);
        return "views/business/mypage";
    }
  
   
}
