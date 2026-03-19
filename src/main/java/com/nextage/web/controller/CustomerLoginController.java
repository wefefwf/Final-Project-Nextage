package com.nextage.web.controller;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nextage.web.domain.BusinessDTO;
import com.nextage.web.domain.CustomerDTO;
import com.nextage.web.service.CustomerService;
import com.nextage.web.userDetails.BusinessUserDetails;
import com.nextage.web.userDetails.CustomerUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class CustomerLoginController {

    private final CustomerService customerService;

    @GetMapping("/customer/login")
    public String customerLogin() {
        return "views/login/customer-login";
    }
    
    @GetMapping("/customer/join")
    public String customerJoin() {
        return "views/login/customer-join";
    }



    
	    @PreAuthorize("hasRole('CUSER')")
	    @GetMapping("/customer/mypage")
    public String mypage(@AuthenticationPrincipal CustomerUserDetails customerUserDetails, Model model) {
	    CustomerDTO customer = customerService.getCustomerByLoginId(customerUserDetails.getUsername());
	    
	    CustomerUserDetails newUserDetails = new CustomerUserDetails(customer);
	    Authentication newAuth = new UsernamePasswordAuthenticationToken(
	            newUserDetails,
	            SecurityContextHolder.getContext().getAuthentication().getCredentials(),
	            newUserDetails.getAuthorities()
	    );
	    SecurityContextHolder.getContext().setAuthentication(newAuth);


        model.addAttribute("customer", customer);
        return "views/login/customer-mypage";
    }
    
	    @PostMapping("/customer/mypage/editProc")
	    public String editProfileProc(CustomerDTO dto, @AuthenticationPrincipal CustomerUserDetails userDetails) {
	        

	        dto.setLoginId(userDetails.getUsername());

	        if (dto.getEmail() != null && dto.getEmailDomain() != null) {
	            dto.setEmail(dto.getEmail() + "@" + dto.getEmailDomain());
	        }

	        if (dto.getAddress() != null && !dto.getAddress().isEmpty()) {
	            dto.setAddress(dto.getPostcode() + "#" + dto.getAddress() + "#" + dto.getAddressDetail());
	        }

	        customerService.updateCustomer(dto);

	        //  시큐리티 세션갱신 
	        CustomerDTO updatedCustomer = customerService.getCustomerByLoginId(dto.getLoginId());
	        CustomerUserDetails newUserDetails = new CustomerUserDetails(updatedCustomer);
	        
	        Authentication newAuth = new UsernamePasswordAuthenticationToken(
	                newUserDetails, 
	                SecurityContextHolder.getContext().getAuthentication().getCredentials(), 
	                newUserDetails.getAuthorities()
	        );
	        SecurityContextHolder.getContext().setAuthentication(newAuth);

	        return "redirect:/customer/mypage"; 
	    }
	    
	    @PostMapping("/customer/mypage/withdrawProc")
	    public String withdrawProc(@AuthenticationPrincipal CustomerUserDetails userDetails,
	                               HttpServletRequest request,
	                               HttpServletResponse response) {
	        customerService.withdraw(userDetails.getUsername());
	        
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        if (auth != null) {
	            new SecurityContextLogoutHandler().logout(request, response, auth);
	        }
	        
	        return "redirect:/customer/main";
	    }
	    
	    
	    
	    @PostMapping("/auth/customer/registerProc")
	    public String joinProc(CustomerDTO dto,@RequestParam("emailDomain") String emailDomain,
	    		@RequestParam(value = "postcode", required = false, defaultValue = "") String postcode,
	            @RequestParam(value = "addressDetail", required = false, defaultValue = "") String addressDetail
	    		) {
	    	
	    	String fullEmail = dto.getEmail() + "@" + emailDomain;
	        
	        dto.setEmail(fullEmail);
	        
	        String baseAddress = dto.getAddress(); 

	      
	        if (baseAddress != null && !baseAddress.trim().isEmpty()) {

	            String fullAddress = postcode + "#" + baseAddress + "#" + addressDetail;
	            
	            dto.setAddress(fullAddress);
	        } else {

	            dto.setAddress(null); 
	        }
	        
	        customerService.register(dto);
	        return "redirect:/customer/login?success"; 
	    }
	    
	    @GetMapping("/auth/customer/check")
	    @ResponseBody
	    public boolean checkId(@RequestParam("loginId") String loginId) {
	        return customerService.isIdDuplicate(loginId);
	    }
	    
	    @GetMapping("/auth/customer/check/name")
	    @ResponseBody
	    public boolean checkName(@RequestParam("nickname") String nickname) {
	        return customerService.isNameDuplicate(nickname);
	    }
	    
	    @GetMapping("/auth/customer/check/phone")
	    @ResponseBody
	    public boolean checkPhone(@RequestParam("phoneNumber") String phoneNumber) {
	        return customerService.isPhoneDuplicate(phoneNumber);
	    }
	    
	    @GetMapping("/auth/customer/check/email")
	    @ResponseBody
	    public boolean checkEmail(@RequestParam("email") String email) {
	        return customerService.isEmailDuplicate(email);
	    }
   
}
