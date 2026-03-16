package com.nextage.web.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderItemsDTO {
    private Long   orderItemId;
    private Long   orderId;
    private Long   kitId;
    private Long   businessId;   // 추가 - 리뷰 등록 시 사용
    private String productName;
    private String imageUrl;
    private int    quantity;
    private int    price;
    private boolean reviewed;
    private LocalDateTime createdAt;
}