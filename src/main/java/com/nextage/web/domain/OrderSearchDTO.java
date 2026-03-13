package com.nextage.web.domain;

import lombok.Data;

//com/nextage/web/domain/OrderSearchDTO.java
@Data
public class OrderSearchDTO {
 private String keyword;      // loginId, nickname, phoneNumber, orderId 통합검색
 private String searchType;   // "loginId", "nickname", "phone", "orderId"
 private String startDate;    // 시작일 (yyyy-MM-dd)
 private String endDate;      // 종료일 (yyyy-MM-dd)
 // 빠른 기간 선택
 private String period;       // "1w", "1m", "3m"
 private int page = 1;
}
