package com.nextage.web.controller;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.OrderSearchDTO;
import com.nextage.web.service.BusinessOrderHistoryService;
import com.nextage.web.userDetails.BusinessUserDetails;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/business/order")
@RequiredArgsConstructor
public class BusinessOrderHistoryController {

    private final BusinessOrderHistoryService service;

    @GetMapping("/history")
    public String historyPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @ModelAttribute OrderSearchDTO search,
            @AuthenticationPrincipal BusinessUserDetails userDetails,
            Model model) {

    	if (userDetails == null) return "redirect:/business/login";
    	
        Long businessId = userDetails.getBusinessId();
        String role     = userDetails.getRole();

        if (search.getPeriod() != null && !search.getPeriod().isEmpty()) {
            LocalDate end   = LocalDate.now();
            LocalDate start = switch (search.getPeriod()) {
                case "1w" -> end.minusWeeks(1);
                case "1m" -> end.minusMonths(1);
                case "3m" -> end.minusMonths(3);
                default   -> null;
            };
            if (start != null) {
                search.setStartDate(start.toString());
                search.setEndDate(end.toString());
            }
        }

        List<OrderHistoryDTO> pendingOrders  = service.getPendingOrders(businessId, role);
        List<OrderHistoryDTO> acceptedOrders = service.getAcceptedOrders(businessId, role, search, page);
        int totalPages = service.getTotalPages(businessId, role, search);

        model.addAttribute("pendingOrders",  pendingOrders);
        model.addAttribute("acceptedOrders", acceptedOrders);
        model.addAttribute("currentPage",    page);
        model.addAttribute("totalPages",     totalPages);
        model.addAttribute("search",         search);
        model.addAttribute("isAdmin", "BADMIN".equals(role));
        model.addAttribute("companyName", userDetails.getBusiness().getCompanyName());
        return "views/orderhistory/business-order-history";
    }

    @PostMapping("/accept/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> acceptOrder(@PathVariable("orderId") Long orderId) {
        try {
            service.acceptOrder(orderId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/reject/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejectOrder(@PathVariable("orderId") Long orderId) {
        try {
            service.rejectOrder(orderId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/delivery/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateDelivery(
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, Integer> body) {
        try {
            service.updateDeliveryStatus(orderId, body.get("deliveryStatus"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @GetMapping("/detail/{orderId}")
    public String detailPage(@PathVariable("orderId") Long orderId,
                             @AuthenticationPrincipal BusinessUserDetails userDetails,
                             Model model) {
        // BADMIN은 전체 접근, BUSER는 본인 주문만
        OrderHistoryDTO order = service.getOrderDetail(orderId);
        if (order == null) return "redirect:/business/order/history";
        model.addAttribute("order", order);
        return "views/orderhistory/business-order-detail"; // 상세 페이지 뷰
    }
    
    @GetMapping("/chat/enter")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> enterChat(
            @RequestParam("orderId") Long orderId,
            @AuthenticationPrincipal BusinessUserDetails userDetails) {

        OrderHistoryDTO order = service.getOrderDetail(orderId);
        if (order.getRoomId() != null) {
            return ResponseEntity.ok(Map.of("roomId", order.getRoomId()));
        }

        // 채팅방 생성
        Long roomId = service.getOrCreateChatRoom(orderId);
        return ResponseEntity.ok(Map.of("roomId", roomId));
    }
    
    @GetMapping("/chat/unread/{roomId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @PathVariable("roomId") Long roomId) {
        int count = service.getUnreadCount(roomId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}