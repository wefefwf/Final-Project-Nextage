package com.nextage.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // ✅ 업체: 의뢰 수락 → SELECTED
    @PostMapping("/accept/{requestId}")
    @ResponseBody
    public ResponseEntity<String> acceptRequest(@PathVariable Long requestId) {
        try {
            requestService.updateRequestStatusByBusiness(requestId, "SELECTED");
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
    }

    // ✅ 업체: 의뢰 거절 → OPEN 복귀
    @PostMapping("/reject/{requestId}")
    @ResponseBody
    public ResponseEntity<String> rejectRequest(@PathVariable Long requestId) {
        try {
            requestService.updateRequestStatusByBusiness(requestId, "OPEN");
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
    }

    // ✅ 업체: 작업 완료 → COMPLETED
    @PostMapping("/complete/{requestId}")
    @ResponseBody
    public ResponseEntity<String> completeRequest(@PathVariable Long requestId) {
        try {
            requestService.updateRequestStatusByBusiness(requestId, "COMPLETED");
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
        }
    }
}