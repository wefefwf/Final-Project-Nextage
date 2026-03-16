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
public class KitDTO {

    private Long kitId;          // kit_id
    private String name;         // 상품명
    private String manufacturer; // 제조사
    private int price;           // 판매가
    private int stock;           // 재고수량
    private String mainImage1;   // 대표 이미지1
    private String mainImage2;   // 대표 이미지2 (선택)
    private String detailImage;  // 상세 이미지
    private LocalDateTime createdAt; // 등록일
    private String status; //상태
}