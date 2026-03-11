package com.nextage.web.domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderHistoryDTO {
    private Long          orderId;
    private String        orderNo;
    private String        impUid;
    private int           totalAmount;
    private String        paymentStatus;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
}