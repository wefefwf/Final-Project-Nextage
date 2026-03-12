package com.nextage.web.controller;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.service.BusinessOrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/business/order")
@RequiredArgsConstructor
public class BusinessOrderHistoryController {

    private final BusinessOrderHistoryService service;
    private static final Long TEMP_BUSINESS_ID = 1L;

    @GetMapping("/history")
    public String historyPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {
        List<OrderHistoryDTO> pendingOrders  = service.getPendingOrders(TEMP_BUSINESS_ID);
        List<OrderHistoryDTO> acceptedOrders = service.getAcceptedOrders(TEMP_BUSINESS_ID, page);
        int totalPages = service.getTotalPages(TEMP_BUSINESS_ID);

        model.addAttribute("pendingOrders",  pendingOrders);
        model.addAttribute("acceptedOrders", acceptedOrders);
        model.addAttribute("currentPage",    page);
        model.addAttribute("totalPages",     totalPages);
        return "views/orderhistory/business-order-history";
    }

    @PostMapping("/accept/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> acceptOrder(
            @PathVariable("orderId") Long orderId) {
        try {
            service.acceptOrder(orderId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/reject/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rejectOrder(
            @PathVariable("orderId") Long orderId) {
        try {
            service.rejectOrder(orderId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/delivery/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateDelivery(
            @PathVariable("orderId") Long orderId,
            @RequestBody Map<String, Integer> body) {
        try {
            int deliveryStatus = body.get("deliveryStatus");
            service.updateDeliveryStatus(orderId, deliveryStatus);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage()));
        }
    }
}