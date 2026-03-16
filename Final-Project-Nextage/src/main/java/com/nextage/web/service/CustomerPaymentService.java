package com.nextage.web.service;

import com.nextage.web.domain.OrderItemsDTO;
import com.nextage.web.domain.PaymentDTO;
import com.nextage.web.mapper.CustomerCartMapper;
import com.nextage.web.mapper.CustomerOrderHistoryMapper;
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

    private final CustomerOrderMapper        orderMapper;
    private final CustomerCartMapper         customerCartMapper;
    private final CustomerOrderHistoryMapper customerOrderHistoryMapper;

    @Transactional
    public String createOrder(Long customerId, int totalAmount) {
        String orderNo = "ORDER_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "_" + (int)(Math.random() * 9000 + 1000);
        Long businessId = 1L;
        orderMapper.insertOrder(orderNo, customerId, businessId, totalAmount);
        log.info("주문 생성 - orderNo: {}, customerId: {}, totalAmount: {}",
                orderNo, customerId, totalAmount);
        return orderNo;
    }

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

        // 3. 결제 성공 처리
        orderMapper.updatePaymentStatus(dto.getOrderNo(), dto.getImpUid(), "PAID");

        // 4. order_items 저장
        if (dto.getOrderItems() != null && !dto.getOrderItems().isEmpty()) {
            Long orderId = orderMapper.selectOrderIdByOrderNo(dto.getOrderNo());
            for (OrderItemsDTO item : dto.getOrderItems()) {
                item.setOrderId(orderId);

                // kitId가 JS에서 문자열로 넘어올 수 있어서 null/빈값 처리
                if (item.getKitId() == null) {
                    log.warn("kitId가 null입니다. productName: {}", item.getProductName());
                }

                log.info("order_item 저장 - orderId: {}, kitId: {}, qty: {}, price: {}",
                        orderId, item.getKitId(), item.getQuantity(), item.getPrice());

                customerOrderHistoryMapper.insertOrderItems(item);
            }
        }

        // 5. 장바구니 아이템 삭제
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
}