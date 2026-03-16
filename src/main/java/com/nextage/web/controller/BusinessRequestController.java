package com.nextage.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.service.CustomerRequestService;

@Controller
@RequestMapping("/business/request")
public class BusinessRequestController {

    @Autowired
    private CustomerRequestService requestService;

    @GetMapping("/list")
    public String requestList(@RequestParam(value = "category", required = false) String category, Model model) {
        List<RequestDTO> list;

        if (category != null && !category.isEmpty()) {
            list = requestService.getRequestsByCategory(category);
        } else {
            list = requestService.getAllRequests();
        }

        model.addAttribute("requestList", list);
        model.addAttribute("currentCategory", category);

        return "views/request/business-requestList";
    }

    @GetMapping("/detail/{requestId}")
    public String requestDetail(@PathVariable("requestId") Long requestId, Model model) {
        RequestDTO request = requestService.getRequestDetail(requestId);
        model.addAttribute("request", request);
        return "views/request/business-requestDetail";
    }
}
