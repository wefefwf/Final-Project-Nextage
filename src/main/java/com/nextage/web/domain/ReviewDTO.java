package com.nextage.web.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    private Long reviewId;       // review_id
    private Long orderItemId;    // order_item_id (FK)
    private Long customerId;     // customer_id (FK)
    private Long businessId;     // business_id (FK)
    private String content;      // 리뷰 내용
    
    // 이미지 경로 (최대 3개)
    private String image1;       // 필수
    private String image2;       // 선택
    private String image3;       // 선택
    
    private String status;       // 상태 (ACTIVE 등)
    private LocalDateTime createdAt; // 생성일
}