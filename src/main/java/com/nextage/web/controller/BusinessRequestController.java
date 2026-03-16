package com.nextage.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // ✅ 추가
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.service.CustomerRequestService;
import com.nextage.web.userDetails.BusinessUserDetails; // ✅ 추가

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
    public String requestDetail(@PathVariable("requestId") Long requestId, 
                                @AuthenticationPrincipal BusinessUserDetails userDetails, // ✅ 추가
                                Model model) {
        RequestDTO request = requestService.getRequestDetail(requestId);
        model.addAttribute("request", request);
        
        // ✅ CUSTOMER와 동일하게 관리자 여부 추가
        boolean isAdmin = userDetails != null && "BADMIN".equals(userDetails.getRole());
        model.addAttribute("isAdmin", isAdmin);
        
        return "views/request/business-requestDetail";
    }

    // ✅ 7. 의뢰 삭제 (CUSTOMER 컨트롤러와 동일한 AJAX 응답 방식)
    @DeleteMapping("/{requestId}")
    @ResponseBody
    public ResponseEntity<String> deleteRequest(@PathVariable("requestId") Long requestId,
                                                @AuthenticationPrincipal BusinessUserDetails userDetails) {
        try {
            RequestDTO request = requestService.getRequestDetail(requestId);
            boolean isAdmin = userDetails != null && "BADMIN".equals(userDetails.getRole());
            
            // ✅ BADMIN 권한 확인 후 삭제(HIDDEN 처리)
            if (request != null && isAdmin) {
                requestService.removeRequest(requestId);
                return ResponseEntity.ok("success");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("fail");
        }
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