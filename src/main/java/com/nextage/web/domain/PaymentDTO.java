package com.nextage.web.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    // ── 아임포트 응답값 ──────────────────────────────────
    private String impUid;          // imp_uid
    private String orderNo;         // order_no (= merchant_uid)
    private int    totalAmount;     // total_amount
    private String paymentStatus;   // payment_status: READY / PAID / FAILED

    // ── 주문 생성 시 필요 ────────────────────────────────
    private Long         customerId;    // customer_id
    private List<String> cartItemIds;   // 결제한 cart_item_id 목록
    
    private List<OrderItemDTO> orderItems;
}