package com.nextage.web.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.service.CustomerRequestService;
import com.nextage.web.userDetails.CustomerUserDetails;

@Controller
public class CustomerMainController {

    @Autowired
    private CustomerRequestService requestService;

    @GetMapping("/customer/main")
    public String main(@AuthenticationPrincipal CustomerUserDetails userDetails, Model model) {

        model.addAttribute("urgentRequests", requestService.getUrgentRequests());
        model.addAttribute("newRequests", requestService.getNewRequests());
        model.addAttribute("bestReviews", requestService.getBestReviews());

        if (userDetails == null) {
            model.addAttribute("myRequests", new ArrayList<>());
            return "views/main/customer-main";
        }

        Long customerId = userDetails.getCustomerId();
        List<RequestDTO> myRequests = requestService.getRequestsByCustomerId(customerId);
        model.addAttribute("myRequests", myRequests != null ? myRequests : new ArrayList<>());
        model.addAttribute("nickname", userDetails.getCustomer().getNickname());

        return "views/main/customer-main";
    }
}