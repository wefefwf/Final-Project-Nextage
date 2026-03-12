package com.nextage.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.service.CustomerRequestService;

@Controller
public class BusinessMainController {

    @Autowired
    private CustomerRequestService requestService;

    @GetMapping("/business/main")
    public String main(Model model) {
        List<RequestDTO> newPostList = requestService.getAllRequests();
        // 최신 5개만 잘라서 넘기기
        if (newPostList.size() > 5) {
            newPostList = newPostList.subList(0, 5);
        }
        model.addAttribute("newPostList", newPostList);
        return "views/main/business-main";
    }
}