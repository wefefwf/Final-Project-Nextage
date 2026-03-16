package com.nextage.web.controller;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.OrderSearchDTO;
import com.nextage.web.service.CustomerOrderHistoryService;
import com.nextage.web.userDetails.CustomerUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customer/admin/order")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CustomerAdminOrderHistoryController {

    // ✅ BusinessOrderHistoryService → CustomerOrderHistoryService 로 교체
    private final CustomerOrderHistoryService service;

    @GetMapping("/history")
    public String historyPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @ModelAttribute OrderSearchDTO search,
            @AuthenticationPrincipal CustomerUserDetails userDetails,
            Model model) {

        if (userDetails == null) return "redirect:/login";

        // 빠른 기간 선택 처리
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

        Long customerId = userDetails.getCustomerId();

        // CADMIN → CustomerOrderHistoryService.getOrderHistory 내부에서 selectAllOrders 호출
        List<OrderHistoryDTO> orders = service.getOrderHistory(customerId, "CADMIN", search, page);
        int totalPages = service.getTotalPages(customerId, "CADMIN", search);

        // 새로 들어온 주문 (PENDING) - Kit 구매만
        List<OrderHistoryDTO> pendingOrders = service.getPendingOrders();

        model.addAttribute("orders",        orders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("currentPage",   page);
        model.addAttribute("totalPages",    totalPages);
        model.addAttribute("search",        search);

        return "views/orderhistory/customer-admin-order-history";
    }

    /* 주문 상세 */
    @GetMapping("/detail/{orderId}")
    public String detailPage(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal CustomerUserDetails userDetails,
            Model model) {

        if (userDetails == null) return "redirect:/login";

        OrderHistoryDTO order = service.getOrderDetail(orderId, userDetails.getCustomerId(), "CADMIN");
        if (order == null) return "redirect:/customer/admin/order/history";

        model.addAttribute("order", order);
        return "views/orderhistory/customer-order-detail";
    }

    /* 배송 상태 변경 */
    @PostMapping("/delivery/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateDelivery(
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, Integer> body,
            @AuthenticationPrincipal CustomerUserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "권한 없음"));
        try {
            service.updateDeliveryStatus(orderId, body.get("deliveryStatus"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* 주문 수락 */
    @PostMapping("/accept/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> acceptOrder(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal CustomerUserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "권한 없음"));
        try {
            service.acceptOrder(orderId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* 주문 거절 */
    @PostMapping("/reject/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejectOrder(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal CustomerUserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "권한 없음"));
        try {
            service.rejectOrder(orderId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}