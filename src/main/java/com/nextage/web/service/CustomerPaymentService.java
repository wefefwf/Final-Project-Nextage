package com.nextage.web.service;

import com.nextage.web.domain.PaymentDTO;
import com.nextage.web.mapper.CustomerCartMapper;
import com.nextage.web.mapper.CustomerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerPaymentService {

    private final CustomerOrderMapper orderMapper;
    private final CustomerCartMapper customerCartMapper;  // ✅ 추가

    /**
     * 주문번호(order_no) 생성 + orders 테이블에 READY 상태로 선저장
     */
    @Transactional
    public String createOrder(Long customerId, int totalAmount) {
        String orderNo = "ORDER_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "_" + (int)(Math.random() * 9000 + 1000);

        orderMapper.insertOrder(orderNo, customerId, totalAmount);
        log.info("주문 생성 - orderNo: {}, customerId: {}, totalAmount: {}",
                orderNo, customerId, totalAmount);
        return orderNo;
    }

    /**
     * 결제 완료 후 서버 검증 + payment_status 업데이트
     */
    @Transactional
    public boolean verifyAndComplete(PaymentDTO dto) {
        // 1. DB에서 사전 저장된 total_amount 조회
        Integer savedAmount = orderMapper.selectTotalAmountByOrderNo(dto.getOrderNo());
        if (savedAmount == null) {
            log.warn("존재하지 않는 order_no: {}", dto.getOrderNo());
            return false;
        }

        // 2. 금액 위변조 검증
        if (savedAmount != dto.getTotalAmount()) {
            log.warn("금액 불일치 - DB total_amount: {}, 결제 요청 amount: {}",
                    savedAmount, dto.getTotalAmount());
            orderMapper.updatePaymentStatus(dto.getOrderNo(), dto.getImpUid(), "FAILED");
            return false;
        }

        // 3. 결제 성공 처리 → payment_status = PAID
        // TODO: 운영 전환 시 포트원 REST API로 imp_uid 실조회 후 이중 검증 추가
        orderMapper.updatePaymentStatus(dto.getOrderNo(), dto.getImpUid(), "PAID");

        // 4. 결제한 장바구니 아이템 삭제
        if (dto.getCartItemIds() != null && !dto.getCartItemIds().isEmpty()) {
            List<Long> ids = dto.getCartItemIds().stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            customerCartMapper.deleteCartItemsByIdsOnly(ids);
        }

        log.info("결제 완료 - orderNo: {}, impUid: {}, totalAmount: {}",
                dto.getOrderNo(), dto.getImpUid(), dto.getTotalAmount());
        return true;
    }

    /**
     * 결제 완료 페이지용 total_amount 조회
     */
    @Transactional(readOnly = true)
    public Integer getTotalAmountByOrderNo(String orderNo) {
        return orderMapper.selectTotalAmountByOrderNo(orderNo);
    }
}