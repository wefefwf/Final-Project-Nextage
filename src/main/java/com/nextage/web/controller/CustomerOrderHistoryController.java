package com.nextage.web.controller;

import com.nextage.web.domain.KitReviewDTO;
import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.service.CustomerOrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class CustomerOrderHistoryController {

    private final CustomerOrderHistoryService customerOrderHistoryService;
    private static final Long TEMP_CUSTOMER_ID = 1L;

    @GetMapping("/history")
    public String historyPage(Model model) {
        List<OrderHistoryDTO> orders =
            customerOrderHistoryService.getOrderHistory(TEMP_CUSTOMER_ID);
        model.addAttribute("orders", orders);
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
    public ResponseEntity<Map<String, Object>> writeReview(@RequestBody KitReviewDTO dto) {
        dto.setCustomerId(TEMP_CUSTOMER_ID);
        boolean success = customerOrderHistoryService.writeReview(dto);
        if (success) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "\uD6C4\uAE30\uAC00 \uB4F1\uB85D\uB418\uC5C8\uC2B5\uB2C8\uB2E4."));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "\uC774\uBBF8 \uC791\uC131\uD55C \uD6C4\uAE30\uC785\uB2C8\uB2E4."));
        }
    }
}