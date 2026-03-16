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
public class ScheduleOrderDTO {
	
	private Long orderId;
    private String orderNo;          // 주문번호도 있어야 리스트에 뿌리겠죠?
    private LocalDateTime dueDate;   // 마감일 (달력 점 찍기용)
    private LocalDateTime createdAt; // 주문받은 날짜 (주문일 표시용)
    private String requestTitle;     // 의뢰 제목
    private String customerNickname; // ★ 주문자 이름 (추가)
}