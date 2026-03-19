package com.nextage.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import com.nextage.web.service.BusinessService;
import com.nextage.web.userDetails.BusinessUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    	 BusinessDTO  business = businessService.getBusinessByLoginId(businessUserDetails.getUsername());
    	 
         BusinessUserDetails newUserDetails = new BusinessUserDetails(business);
         
         Authentication newAuth = new UsernamePasswordAuthenticationToken(
                 newUserDetails, 
                 SecurityContextHolder.getContext().getAuthentication().getCredentials(), 
                 newUserDetails.getAuthorities()
         );
         SecurityContextHolder.getContext().setAuthentication(newAuth);
         
         model.addAttribute("business", business);
        return "views/login/business-mypage";
    }
    
    
    @PostMapping("/business/mypage/editProc")
    public String editProfileProc(BusinessDTO dto, @AuthenticationPrincipal BusinessUserDetails userDetails) {
        

        dto.setLoginId(userDetails.getUsername());
        businessService.updateBusiness(dto);

        //  시큐리티 세션갱신 
        BusinessDTO updatedBusiness = businessService.getBusinessByLoginId(dto.getLoginId());
        BusinessUserDetails newUserDetails = new BusinessUserDetails(updatedBusiness);
        
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                newUserDetails, 
                SecurityContextHolder.getContext().getAuthentication().getCredentials(), 
                newUserDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        return "redirect:/business/mypage"; 
    }
    
    
    
    @PostMapping("/business/mypage/withdrawProc")
    public String withdrawProc(@AuthenticationPrincipal BusinessUserDetails userDetails,
                               HttpServletRequest request,
                               HttpServletResponse response) {
    	businessService.withdraw(userDetails.getUsername());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        
        return "redirect:/business/main";
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
