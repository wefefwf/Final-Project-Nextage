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
@RequestMapping("/customer/order")
@RequiredArgsConstructor
public class CustomerOrderHistoryController {

    private final CustomerOrderHistoryService service;

    /* ─────────────────────────────────────────
       구매내역 목록 (CUSER + CADMIN 공통)
       ───────────────────────────────────────── */
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

        String role       = userDetails.getRole();
        Long   customerId = userDetails.getCustomerId();

        List<OrderHistoryDTO> orders = service.getOrderHistory(customerId, role, search, page);
        int totalPages = service.getTotalPages(customerId, role, search);

        model.addAttribute("orders",      orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  totalPages);
        model.addAttribute("search",      search);
        model.addAttribute("isAdmin",     "CADMIN".equals(role));
        model.addAttribute("nickname", userDetails.getCustomer().getNickname());

        return "views/orderhistory/customer-order-history";
    }

    /* ─────────────────────────────────────────
       주문 상세 (CUSER + CADMIN 공통)
       ───────────────────────────────────────── */
    @GetMapping("/detail/{orderId}")
    public String detailPage(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal CustomerUserDetails userDetails,
            Model model) {

        if (userDetails == null) return "redirect:/login";

        String role       = userDetails.getRole();
        Long   customerId = userDetails.getCustomerId();

        OrderHistoryDTO order = service.getOrderDetail(orderId, customerId, role);
        if (order == null) return "redirect:/customer/order/history";

        model.addAttribute("order", order);
        return "views/orderhistory/customer-order-detail";
    }

    /* ─────────────────────────────────────────
       후기 작성
       ───────────────────────────────────────── */
	/*
	 * @PostMapping("/review")
	 * 
	 * @ResponseBody public ResponseEntity<Map<String, Object>> writeReview(
	 * 
	 * @RequestBody com.nextage.web.domain.ReviewDTO dto,
	 * 
	 * @AuthenticationPrincipal CustomerUserDetails userDetails) {
	 * 
	 * if (userDetails == null) return
	 * ResponseEntity.status(401).body(Map.of("success", false, "message",
	 * "로그인 필요"));
	 * 
	 * dto.setCustomerId(userDetails.getCustomerId()); boolean ok =
	 * service.writeReview(dto); if (ok) return ResponseEntity.ok(Map.of("success",
	 * true)); return ResponseEntity.badRequest().body(Map.of("success", false,
	 * "message", "이미 작성한 후기입니다.")); }
	 */

    /* ─────────────────────────────────────────
       구매내역 삭제
       ───────────────────────────────────────── */
    @DeleteMapping("/delete/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteOrder(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal CustomerUserDetails userDetails) {

        if (userDetails == null)
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "로그인 필요"));

        try {
            service.deleteOrder(orderId, userDetails.getCustomerId());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
	/*
	 * @GetMapping("/review/{orderItemId}")
	 * 
	 * @ResponseBody public ResponseEntity<Map<String, Object>> getReview(
	 * 
	 * @PathVariable("orderItemId") Long orderItemId,
	 * 
	 * @AuthenticationPrincipal CustomerUserDetails userDetails) { if (userDetails
	 * == null) return ResponseEntity.status(401).body(Map.of("success", false));
	 * 
	 * com.nextage.web.domain.ReviewDTO review = service.getReview(orderItemId); if
	 * (review == null) return ResponseEntity.ok(Map.of("success", false)); return
	 * ResponseEntity.ok(Map.of("success", true, "content", review.getContent(),
	 * "createdAt", review.getCreatedAt().toString())); }
	 * 
	 * @PutMapping("/review/{orderItemId}")
	 * 
	 * @ResponseBody public ResponseEntity<Map<String, Object>> updateReview(
	 * 
	 * @PathVariable("orderItemId") Long orderItemId,
	 * 
	 * @RequestBody Map<String, String> body,
	 * 
	 * @AuthenticationPrincipal CustomerUserDetails userDetails) {
	 * 
	 * if (userDetails == null) return
	 * ResponseEntity.status(401).body(Map.of("success", false, "message",
	 * "로그인 필요"));
	 * 
	 * try { service.updateReview(orderItemId, body.get("content")); return
	 * ResponseEntity.ok(Map.of("success", true)); } catch (Exception e) { return
	 * ResponseEntity.badRequest().body(Map.of("success", false, "message",
	 * e.getMessage())); } }
	 */
    
    @GetMapping("/chat/enter")
    public String enterChat(
            @RequestParam("orderId") Long orderId,
            @AuthenticationPrincipal CustomerUserDetails userDetails) {

        if (userDetails == null) return "redirect:/login";

        Long roomId = service.getOrCreateChatRoom(orderId, userDetails.getCustomerId());
        return "redirect:/chat?roomId=" + roomId;
    }
}