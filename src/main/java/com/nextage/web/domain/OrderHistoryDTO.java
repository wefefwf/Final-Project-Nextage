package com.nextage.web.domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderHistoryDTO {
    private Long          orderId;
    private String        orderNo;
    private String        impUid;
    private Long          businessId;    // 추가 - review.business_id에 사용
    private Long          bidId;         // 추가 - null이면 kit구매, not null이면 개인거래
    private int           totalAmount;
    private String        paymentStatus;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;

    // bid_id가 있으면 개인 대 개인 거래
    public boolean isPersonalOrder() {
        return bidId != null;
    }
}