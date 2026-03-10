package com.nextage.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // [해결] 이 줄을 반드시 추가하세요!
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.service.CustomerRequestService;

@Controller
@RequestMapping("/customer/request")
public class CustomerRequestController { // [참고] 오타 Cutomer -> Customer 수정 권장

    @Autowired
    private CustomerRequestService requestService;

    @GetMapping("/write")
    public String writeForm() {
        // [해결] 파일 경로 확인 (templates/ 생략)
        return "views/request/customer-requestForm"; 
    }

    @GetMapping("/list")
    public String requestList(Model model) {
        List<RequestDTO> list = requestService.getAllRequests(); 
        model.addAttribute("requestList", list);
        return "views/request/customer-requestList"; 
    }

    @PostMapping("/insert")
    public String insertRequest(RequestDTO dto, 
                                @RequestParam(value = "files", required = false) MultipartFile[] files) {
        dto.setCustomerId(1L); 
        requestService.registerRequest(dto, files);
        return "redirect:/customer/request/list"; // [해결] 절대 경로 리다이렉트
    }
    
 // 의뢰 상세 보기
    @GetMapping("/detail/{requestId}") //승지언니랑 맞춰야할 곳 
    public String requestDetail(@PathVariable("requestId") Long requestId, Model model) {
        // DB에서 상세 데이터 가져오기 (서비스에 해당 메서드 구현 필요)
        RequestDTO request = requestService.getRequestDetail(requestId); 
        model.addAttribute("request", request);
        
        return "views/request/customer-requestDetail"; 
    }
}