package com.nextage.web.domain;

import lombok.Data;

@Data
public class ReviewDTO {
    private Long   reviewId;
    private Long   orderItemId;
    private Long   customerId;
    private Long   businessId;   // orders.business_id에서 가져옴
    private String content;
    private String image1;
    private String status;
}