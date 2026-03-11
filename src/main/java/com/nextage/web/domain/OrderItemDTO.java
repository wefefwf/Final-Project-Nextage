package com.nextage.web.domain;

import lombok.Data;

@Data
public class OrderItemDTO {
    private Long   orderItemId;
    private Long   orderId;
    private Long   kitId;
    private String productName;
    private String imageUrl;
    private int    quantity;
    private int    price;
    private boolean reviewed;
}