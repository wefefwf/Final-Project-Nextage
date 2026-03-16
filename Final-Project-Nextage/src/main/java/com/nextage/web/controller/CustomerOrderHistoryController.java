package com.nextage.web.controller;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.ReviewDTO;
import com.nextage.web.service.CustomerOrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customer/order")  // /order → /order/detail 로 변경
@RequiredArgsConstructor
public class CustomerOrderHistoryController {

    private final CustomerOrderHistoryService customerOrderHistoryService;
    private static final Long TEMP_CUSTOMER_ID = 1L;

    private static final int PAGE_SIZE = 5;
    
    @GetMapping("/history")
    public String historyPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {

        List<OrderHistoryDTO> orders =
            customerOrderHistoryService.getOrderHistory(
                    TEMP_CUSTOMER_ID, page);

        int totalPages =
            customerOrderHistoryService.getTotalPages(
                    TEMP_CUSTOMER_ID);

        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "views/orderhistory/customer-order-history";
    }

    @GetMapping("/detail/{orderId}")
    public String detailPage(@PathVariable("orderId") Long orderId, Model model) {
        OrderHistoryDTO order = customerOrderHistoryService.getOrderDetail(orderId);
        model.addAttribute("order", order);
        return "views/orderhistory/customer-order-detail";
    }

    @PostMapping("/review")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> writeReview(@RequestBody ReviewDTO dto) {
        dto.setCustomerId(TEMP_CUSTOMER_ID);
        if (dto.getBusinessId() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, "message", "잘못된 요청입니다."));
        }
        boolean success = customerOrderHistoryService.writeReview(dto);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "후기가 등록되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, "message", "이미 작성한 후기입니다."));
        }
    }

    @DeleteMapping("/delete/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteOrder(
            @PathVariable("orderId") Long orderId) {
        try {
            customerOrderHistoryService.deleteOrder(orderId, TEMP_CUSTOMER_ID);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, "message", e.getMessage()));
        }
    }
}