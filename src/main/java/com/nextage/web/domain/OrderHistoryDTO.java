package com.nextage.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderHistoryDTO {
    private Long          orderId;
    private String        orderNo;
    private String        impUid;
    private Long          businessId;
    private Long          bidId;
    private Long          requestId;      // ✅ 추가 - 입찰 페이지 이동용
    private int           totalAmount;
    private String        paymentStatus;
    private int unreadCount;
    private String businessNickname;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private List<OrderItemsDTO> items;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate     bidExpectedDueDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dueDate;

    // 비즈니스 주문관리용 추가 필드
    private String        customerNickname;
    private Long          customerId;
    private Long          roomId;
    private int           deliveryStatus;
    private String        acceptStatus;

    public boolean isPersonalOrder() { return bidId != null; }
    public boolean isCancelled()     { return deliveryStatus == 9; }
    public boolean isPending()       { return "PENDING".equals(acceptStatus); }
    public boolean isAccepted()      { return "ACCEPTED".equals(acceptStatus); }
    public boolean isRejected()      { return "REJECTED".equals(acceptStatus); }
}