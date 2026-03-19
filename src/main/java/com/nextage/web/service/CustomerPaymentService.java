package com.nextage.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextage.web.domain.OrderItemsDTO;
import com.nextage.web.domain.PaymentDTO;
import com.nextage.web.mapper.CustomerCartMapper;
import com.nextage.web.mapper.CustomerOrderHistoryMapper;
import com.nextage.web.mapper.CustomerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerPaymentService {

    private final CustomerOrderMapper        orderMapper;
    private final CustomerCartMapper         customerCartMapper;
    private final CustomerOrderHistoryMapper customerOrderHistoryMapper;

    // ── application.properties 에 추가 필요 ──────────────────────
    // portone.api.secret=포트원_V2_API_SECRET
    @Value("${portone.api.secret}")
    private String portoneApiSecret;

    private static final String PORTONE_BASE = "https://api.portone.io";

    // RestTemplate / ObjectMapper - 새 클래스 없이 직접 생성
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper  = new ObjectMapper();

    // ─────────────────────────────────────────────────────────────
    //  기존 메서드 (변경 없음)
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public String createOrder(Long customerId, int totalAmount) {
        String orderNo = "ORDER_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "_" + (int)(Math.random() * 9000 + 1000);
        orderMapper.insertOrder(orderNo, customerId, null, totalAmount, null);
        log.info("주문 생성 - orderNo: {}, customerId: {}, totalAmount: {}",
                orderNo, customerId, totalAmount);
        return orderNo;
    }

    @Transactional
    public boolean verifyAndComplete(PaymentDTO dto) {
        Integer savedAmount = orderMapper.selectTotalAmountByOrderNo(dto.getOrderNo());
        if (savedAmount == null) {
            log.warn("존재하지 않는 order_no: {}", dto.getOrderNo());
            return false;
        }
        if (savedAmount != dto.getTotalAmount()) {
            log.warn("금액 불일치 - DB: {}, 요청: {}", savedAmount, dto.getTotalAmount());
            orderMapper.updatePaymentStatus(dto.getOrderNo(), dto.getImpUid(), "FAILED");
            return false;
        }

        orderMapper.updatePaymentStatus(dto.getOrderNo(), dto.getImpUid(), "PAID");

        if (dto.getOrderItems() != null && !dto.getOrderItems().isEmpty()) {
            Long orderId = orderMapper.selectOrderIdByOrderNo(dto.getOrderNo());
            for (OrderItemsDTO item : dto.getOrderItems()) {
                item.setOrderId(orderId);
                if (item.getKitId() == null) {
                    log.warn("kitId가 null입니다. productName: {}", item.getProductName());
                }
                log.info("order_item 저장 - orderId: {}, kitId: {}, qty: {}, price: {}",
                        orderId, item.getKitId(), item.getQuantity(), item.getPrice());
                customerOrderHistoryMapper.insertOrderItems(item);
            }
        }

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

    @Transactional(readOnly = true)
    public Integer getTotalAmountByOrderNo(String orderNo) {
        return orderMapper.selectTotalAmountByOrderNo(orderNo);
    }

    // ─────────────────────────────────────────────────────────────
    //  신규: 관리자 결제 취소 / 주문 거절
    // ─────────────────────────────────────────────────────────────

    /**
     * 결제 취소 (PAID → CANCELLED).
     * 포트원 v2 취소 API 호출 후 DB 상태 업데이트.
     */
    @Transactional
    public boolean cancelPayment(String orderNo, String reason) {
        // 1. 현재 상태 확인
        String status = orderMapper.selectPaymentStatusByOrderNo(orderNo);
        if (!"PAID".equals(status)) {
            log.warn("취소 불가 - 현재 상태: {}, orderNo: {}", status, orderNo);
            return false;
        }

        // 2. paymentId(imp_uid) 조회
        String paymentId = orderMapper.selectImpUidByOrderNo(orderNo);
        if (paymentId == null || paymentId.isBlank()) {
            log.warn("paymentId 없음 - orderNo: {}", orderNo);
            return false;
        }

        // 3. 취소 금액 (전액)
        Integer totalAmount = orderMapper.selectTotalAmountByOrderNo(orderNo);

        // 4. 포트원 v2 취소 API 호출
        callPortoneCancel(paymentId, totalAmount != null ? totalAmount : 0, reason);

        // 5. DB 상태 업데이트
        orderMapper.updateCancelStatus(orderNo, "CANCELLED");
        log.info("결제 취소 완료 - orderNo: {}, paymentId: {}", orderNo, paymentId);
        return true;
    }

    /**
     * 주문 거절 (READY → REJECTED, PAID → 포트원 취소 후 REJECTED).
     */
    @Transactional
    public boolean rejectOrder(String orderNo, String reason) {
        String status = orderMapper.selectPaymentStatusByOrderNo(orderNo);

        if (status == null) {
            log.warn("존재하지 않는 orderNo: {}", orderNo);
            return false;
        }
        if ("CANCELLED".equals(status) || "REJECTED".equals(status)) {
            log.warn("이미 처리된 주문 - status: {}, orderNo: {}", status, orderNo);
            return false;
        }

        // PAID 상태면 포트원 취소도 함께 처리
        if ("PAID".equals(status)) {
            String  paymentId   = orderMapper.selectImpUidByOrderNo(orderNo);
            Integer totalAmount = orderMapper.selectTotalAmountByOrderNo(orderNo);
            callPortoneCancel(paymentId, totalAmount != null ? totalAmount : 0, reason);
        }

        orderMapper.updateCancelStatus(orderNo, "REJECTED");
        log.info("주문 거절 완료 - orderNo: {}, 이전상태: {}", orderNo, status);
        return true;
    }

    // ─────────────────────────────────────────────────────────────
    //  포트원 v2 취소 API 헬퍼
    //  - v2는 토큰 발급 없이 API Secret을 Bearer로 직접 사용
    //  - URL: POST https://api.portone.io/payments/{paymentId}/cancel
    // ─────────────────────────────────────────────────────────────

    private boolean callPortoneCancel(String paymentId, int cancelAmount, String reason) {
        try {
        	
        	log.info("포트원 시크릿 키 확인: [{}]", portoneApiSecret);
            log.info("취소 요청 - paymentId: {}, amount: {}", paymentId, cancelAmount);
            
            Map<String, Object> body = new HashMap<>();
            body.put("reason", reason != null ? reason : "관리자 취소");
            body.put("amount", cancelAmount);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(portoneApiSecret); // v2: API Secret 직접 사용 (토큰 발급 불필요)

            ResponseEntity<String> response = restTemplate.postForEntity(
                    PORTONE_BASE + "/payments/" + paymentId + "/cancel",
                    new HttpEntity<>(body, headers),
                    String.class
            );

            // v2는 HTTP 200이면 성공
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("포트원 v2 취소 성공 - paymentId: {}, amount: {}", paymentId, cancelAmount);
                return true;
            }

            log.error("포트원 v2 취소 실패 - status: {}, body: {}",
                    response.getStatusCode(), response.getBody());
            return false;

        } catch (Exception e) {
            log.error("포트원 v2 취소 API 호출 중 예외 - paymentId: {}", paymentId, e);
            return false;
        }
    }
}