package com.nextage.web.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nextage.web.domain.RequestDTO;
import com.nextage.web.service.CustomerRequestService;
import com.nextage.web.userDetails.CustomerUserDetails;

@Controller
@RequestMapping("/customer/request")
public class CustomerRequestController {

    @Autowired
    private CustomerRequestService requestService;

    // 1. 의뢰 작성 폼
    @GetMapping("/write")
    public String writeForm() {
        return "views/request/customer-requestForm"; 
    }

    // 2. 의뢰 리스트 (카테고리 필터링 포함)
    @GetMapping("/list")
    public String requestList(@RequestParam(value = "category", required = false) String category,
                              @AuthenticationPrincipal CustomerUserDetails userDetails,
                              Model model) {
        List<RequestDTO> list;
        
        if (category != null && !category.isEmpty()) {
            list = requestService.getRequestsByCategory(category); 
        } else {
            list = requestService.getAllRequests();
        }
        
        model.addAttribute("requestList", list);
        model.addAttribute("currentCategory", category);
        
        // ✅ 관리자 여부 추가
        boolean isAdmin = userDetails != null && "CADMIN".equals(userDetails.getRole());
        model.addAttribute("isAdmin", isAdmin);
        
        return "views/request/customer-requestList"; 
    }

    // 3. ✅ 의뢰 등록
    @PostMapping("/insert")
    public String insertRequest(RequestDTO dto, 
                                @RequestParam(value = "files", required = false) MultipartFile[] files,
                                @AuthenticationPrincipal CustomerUserDetails userDetails) {
        dto.setCustomerId(userDetails.getCustomerId()); 
        requestService.registerRequest(dto, files);
        return "redirect:/customer/request/list";
    }
    
    // 4. 의뢰 상세 보기
    @GetMapping("/detail/{requestId}")
    public String requestDetail(@PathVariable("requestId") Long requestId,
                                @AuthenticationPrincipal CustomerUserDetails userDetails,
                                Model model) {
        RequestDTO request = requestService.getRequestDetail(requestId); 
        model.addAttribute("request", request);

        boolean isOwner = userDetails != null 
                          && request != null 
                          && request.getCustomerId().equals(userDetails.getCustomerId());
        model.addAttribute("isOwner", isOwner);
        
        // ✅ 관리자 여부 추가
        boolean isAdmin = userDetails != null && "CADMIN".equals(userDetails.getRole());
        model.addAttribute("isAdmin", isAdmin);
        
        return "views/request/customer-requestDetail"; 
    }
    
    // 5. ✅ 수정 폼
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") Long requestId, 
                           @AuthenticationPrincipal CustomerUserDetails userDetails,
                           Model model) {
        RequestDTO request = requestService.getRequestDetail(requestId);
        if (request == null || !request.getCustomerId().equals(userDetails.getCustomerId())) {
            return "redirect:/customer/request/list";
        }
        model.addAttribute("request", request);
        return "views/request/customer-requestEdit";
    }

    // 6. ✅ 수정 처리
    @PostMapping("/edit/{id}")
    public String updateRequest(@PathVariable("id") Long requestId,
                                RequestDTO dto,
                                @RequestParam(value = "files", required = false) MultipartFile[] files,
                                @AuthenticationPrincipal CustomerUserDetails userDetails) {
        RequestDTO existingRequest = requestService.getRequestDetail(requestId);
        if (existingRequest != null && existingRequest.getCustomerId().equals(userDetails.getCustomerId())) {
            dto.setRequestId(requestId);
            requestService.updateRequest(dto, files);
        }
        return "redirect:/customer/request/detail/" + requestId;
    }

    // 7. ✅ 의뢰 삭제 (관리자도 삭제 가능하도록 수정)
    @DeleteMapping("/{requestId}")
    @ResponseBody
    public ResponseEntity<String> deleteRequest(@PathVariable("requestId") Long requestId,
                                                @AuthenticationPrincipal CustomerUserDetails userDetails) {
        try {
            RequestDTO request = requestService.getRequestDetail(requestId);
            boolean isAdmin = userDetails != null && "CADMIN".equals(userDetails.getRole());
            
            // ✅ 본인 또는 관리자면 삭제 가능
            if (request != null && (request.getCustomerId().equals(userDetails.getCustomerId()) || isAdmin)) {
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

    // 8. ✅ 의뢰 상태 변경
    @PostMapping("/status/{requestId}")
    public String updateStatus(@PathVariable("requestId") Long requestId,
                               @RequestParam("status") String status,
                               @AuthenticationPrincipal CustomerUserDetails userDetails) {
        try {
            requestService.updateRequestStatus(requestId, status, userDetails.getCustomerId());
        } catch (RuntimeException e) {
            return "redirect:/customer/request/list";
        }
        return "redirect:/customer/request/detail/" + requestId;
    }
}