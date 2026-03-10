package com.nextage.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.service.CustomerRequestService;

@Controller
@RequestMapping("/customer/request")
public class CustomerRequestController {

    @Autowired
    private CustomerRequestService requestService;

    @GetMapping("/write")
    public String writeForm() {
        return "views/request/customer-requestForm"; 
    }

    // [수정] 카테고리 필터링 기능을 추가한 리스트 메서드
    @GetMapping("/list")
    public String requestList(@RequestParam(value = "category", required = false) String category, Model model) {
        List<RequestDTO> list;
        
        // 카테고리가 있으면 필터링, 없으면 전체 리스트 호출
        if (category != null && !category.isEmpty()) {
            list = requestService.getRequestsByCategory(category); 
        } else {
            list = requestService.getAllRequests();
        }
        
        model.addAttribute("requestList", list);
        model.addAttribute("currentCategory", category); 
        
        return "views/request/customer-requestList"; 
    }

    @PostMapping("/insert")
    public String insertRequest(RequestDTO dto, 
                                @RequestParam(value = "files", required = false) MultipartFile[] files) {
        dto.setCustomerId(1L); 
        requestService.registerRequest(dto, files);
        return "redirect:/customer/request/list";
    }
    
    // 의뢰 상세 보기
    @GetMapping("/detail/{requestId}") //승지언니랑 맞춰야할곳
    public String requestDetail(@PathVariable("requestId") Long requestId, Model model) {
        RequestDTO request = requestService.getRequestDetail(requestId); 
        model.addAttribute("request", request);
        
        return "views/request/customer-requestDetail"; 
    }
}