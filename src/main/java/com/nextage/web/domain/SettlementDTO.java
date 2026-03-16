package com.nextage.web.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SettlementDTO {
    private Long   settlementId;
    private Long   businessId;
    private Long   orderId;
    private long   salesAmount;
    private long   commissionAmount;
    private long   settlementAmount;
    private String settlementStatus;
    private LocalDateTime createdAt;

    // 조인 필드
    private String orderNo;
    private String customerNickname;
    private String companyName;
}