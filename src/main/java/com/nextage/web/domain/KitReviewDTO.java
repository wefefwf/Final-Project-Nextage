package com.nextage.web.domain;

import lombok.Data;

@Data
public class KitReviewDTO {
    private Long   reviewId;
    private Long   orderItemId;
    private Long   kitId;
    private Long   customerId;
    private int    rating;
    private String content;
}