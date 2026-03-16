package com.nextage.web.service;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.mapper.BusinessOrderHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessOrderHistoryService {

    private final BusinessOrderHistoryMapper mapper;
    private static final int PAGE_SIZE = 6;

    // 새 주문 (PENDING)
    @Transactional(readOnly = true)
    public List<OrderHistoryDTO> getPendingOrders(Long businessId) {
        List<OrderHistoryDTO> orders = mapper.selectPendingOrders(businessId);
        orders.forEach(o -> o.setItems(mapper.selectOrderItems(o.getOrderId())));
        return orders;
    }

    // 수락된 주문 (ACCEPTED + 페이징)
    @Transactional(readOnly = true)
    public List<OrderHistoryDTO> getAcceptedOrders(Long businessId, int page) {
        int offset = (page - 1) * PAGE_SIZE;
        List<OrderHistoryDTO> orders =
                mapper.selectAcceptedOrders(businessId, offset, PAGE_SIZE);
        orders.forEach(o -> o.setItems(mapper.selectOrderItems(o.getOrderId())));
        return orders;
    }

    @Transactional(readOnly = true)
    public int getTotalPages(Long businessId) {
        int total = mapper.countAcceptedOrders(businessId);
        return (int) Math.ceil((double) total / PAGE_SIZE);
    }

    // 수락
    @Transactional
    public void acceptOrder(Long orderId) {
        mapper.updateAcceptStatus(orderId, "ACCEPTED");
        log.info("주문 수락 - orderId: {}", orderId);
    }

    // 거절
    @Transactional
    public void rejectOrder(Long orderId) {
        mapper.updateAcceptStatus(orderId, "REJECTED");
        log.info("주문 거절 - orderId: {}", orderId);
    }

    // 배송 상태 변경
    @Transactional
    public void updateDeliveryStatus(Long orderId, int deliveryStatus) {
        mapper.updateDeliveryStatus(orderId, deliveryStatus);
        log.info("배송 상태 변경 - orderId: {}, status: {}", orderId, deliveryStatus);
    }
}