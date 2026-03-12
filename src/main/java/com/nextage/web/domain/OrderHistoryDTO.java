package com.nextage.web.domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderHistoryDTO {
    private Long          orderId;
    private String        orderNo;
    private String        impUid;
    private Long          businessId;
    private Long          bidId;
    private int           totalAmount;
    private String        paymentStatus;
    private LocalDateTime createdAt;
    private List<OrderItemsDTO> items;

    // ↓ 비즈니스 주문관리용 추가 필드
    private String        customerNickname;  // 고객 닉네임
    private Long          customerId;
    private Long          roomId;            // 채팅방 ID
    private int           deliveryStatus;    // 1=결제완료, 2=배송중, 3=배송완료, 9=취소
    private LocalDateTime dueDate;
    private String acceptStatus;  // PENDING, ACCEPTED, REJECTED

    public boolean isPersonalOrder() { return bidId != null; }
    public boolean isCancelled()     { return deliveryStatus == 9; }
    public boolean isPending()  { return "PENDING".equals(acceptStatus); }
    public boolean isAccepted() { return "ACCEPTED".equals(acceptStatus); }
    public boolean isRejected() { return "REJECTED".equals(acceptStatus); }
}