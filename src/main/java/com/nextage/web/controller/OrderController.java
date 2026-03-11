package com.nextage.web.controller;

import com.nextage.web.domain.PaymentDTO;
import com.nextage.web.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller                          // ✅ @RestController → @Controller 로 변경
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final PaymentService paymentService;

    // 임시 테스트용 customer_id (로그인 연동 후 제거)
    private static final Long TEMP_CUSTOMER_ID = 1L;

    /**
     * POST /order/payment/create
     * 주문 사전 생성 → order_no 반환
     */
    @PostMapping("/payment/create")
    @ResponseBody                    // ✅ JSON 응답 메서드에 @ResponseBody 추가
    public ResponseEntity<Map<String, String>> createOrder(
            @RequestBody Map<String, Integer> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long customerId  = TEMP_CUSTOMER_ID;
        int  totalAmount = body.get("totalAmount");

        String orderNo = paymentService.createOrder(customerId, totalAmount);
        return ResponseEntity.ok(Map.of("orderNo", orderNo));
    }

    /**
     * POST /order/payment/verify
     * 결제 완료 후 금액 검증 + payment_status 업데이트
     */
    @PostMapping("/payment/verify")
    @ResponseBody                    // ✅ JSON 응답 메서드에 @ResponseBody 추가
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody PaymentDTO dto) {
        boolean success = paymentService.verifyAndComplete(dto);

        if (success) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "결제가 완료되었습니다.",
                "orderNo", dto.getOrderNo()
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "결제 검증에 실패했습니다."
            ));
        }
    }

    /**
     * GET /order/complete
     * 결제 완료 페이지
     */
    @GetMapping("/complete")
    public String completePage(
            @RequestParam("orderNo") String orderNo,  // ✅ name 명시
            org.springframework.ui.Model model) {

        Integer totalAmount = paymentService.getTotalAmountByOrderNo(orderNo);

        model.addAttribute("orderNo",     orderNo);
        model.addAttribute("totalAmount", totalAmount != null ? totalAmount : 0);
        model.addAttribute("redirectUrl", "/customer/shop");
        return "views/shop/customer-payment-complete";
    }
}