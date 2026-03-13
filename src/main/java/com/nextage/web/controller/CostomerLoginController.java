package com.nextage.web.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.nextage.web.domain.BusinessDTO;
import com.nextage.web.domain.CustomerDTO;
import com.nextage.web.userDetails.BusinessUserDetails;
import com.nextage.web.userDetails.CustomerUserDetails;


@Controller
public class CostomerLoginController {

    @GetMapping("/customer/login")
    public String customerLogin() {
        return "views/login/customer-login";
    }


    
	    @PreAuthorize("hasRole('CUSER')")
	    @GetMapping("/customer/mypage")
    public String mypage(@AuthenticationPrincipal CustomerUserDetails customerUserDetails, Model model) {
        CustomerDTO customer = customerUserDetails.getCustomer();
        model.addAttribute("customer", customer);
        return "views/login/customer-mypage";
    }
    

   
}
