package com.nextage.web.service;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.OrderSearchDTO;
import com.nextage.web.domain.ReviewDTO;
import com.nextage.web.mapper.CustomerOrderHistoryMapper;
import com.nextage.web.mapper.CustomerReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerOrderHistoryService {

    private final CustomerOrderHistoryMapper customerOrderHistoryMapper;
    private final CustomerReviewMapper       customerReviewMapper;
    private static final int PAGE_SIZE = 5;

    @Transactional(readOnly = true)
    public List<OrderHistoryDTO> getOrderHistory(Long customerId, String role, OrderSearchDTO search, int page) {
        int offset = (page - 1) * PAGE_SIZE;
        List<OrderHistoryDTO> orders;
        if ("CADMIN".equals(role)) {
            orders = customerOrderHistoryMapper.selectAllOrders(search, offset, PAGE_SIZE);
        } else {
            orders = customerOrderHistoryMapper.selectOrdersByCustomerId(customerId, search, offset, PAGE_SIZE);
        }
        orders.forEach(order ->
            order.setItems(customerOrderHistoryMapper.selectOrderItems(order.getOrderId()))
        );
        return orders;
    }

    @Transactional(readOnly = true)
    public int getTotalPages(Long customerId, String role, OrderSearchDTO search) {
        int total;
        if ("CADMIN".equals(role)) {
            total = customerOrderHistoryMapper.countAllOrders(search);
        } else {
            total = customerOrderHistoryMapper.countOrdersByCustomerId(customerId, search);
        }
        return (int) Math.ceil((double) total / PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public OrderHistoryDTO getOrderDetail(Long orderId, Long customerId, String role) {
        OrderHistoryDTO order = customerOrderHistoryMapper.selectOrderDetail(orderId);
        if (order == null) return null;

        log.info("orderId: {}, order.customerId: {}, 로그인customerId: {}, role: {}",
                orderId, order.getCustomerId(), customerId, role);

        if ("CADMIN".equals(role)) {
            order.setItems(customerOrderHistoryMapper.selectOrderItems(orderId));
            return order;
        }

        if (order.getCustomerId() == null) return null;
        if (!order.getCustomerId().equals(customerId)) return null;

        order.setItems(customerOrderHistoryMapper.selectOrderItems(orderId));
        return order;
    }

    /* ─────────────────────────────────────────
       CADMIN 전용: 배송/수락/거절 처리
       ───────────────────────────────────────── */
    @Transactional(readOnly = true)
    public List<OrderHistoryDTO> getPendingOrders() {
        List<OrderHistoryDTO> orders = customerOrderHistoryMapper.selectPendingOrders();
        orders.forEach(order ->
            order.setItems(customerOrderHistoryMapper.selectOrderItems(order.getOrderId()))
        );
        return orders;
    }

    @Transactional
    public void updateDeliveryStatus(Long orderId, int deliveryStatus) {
        customerOrderHistoryMapper.updateDeliveryStatus(orderId, deliveryStatus);
        log.info("배송 상태 변경 - orderId: {}, status: {}", orderId, deliveryStatus);
    }

    @Transactional
    public void acceptOrder(Long orderId) {
        customerOrderHistoryMapper.updateAcceptStatus(orderId, "ACCEPTED");
        customerOrderHistoryMapper.updateDeliveryStatus(orderId, 2);
        log.info("주문 수락 - orderId: {}", orderId);
    }

    @Transactional
    public void rejectOrder(Long orderId) {
        customerOrderHistoryMapper.updateAcceptStatus(orderId, "REJECTED");
        customerOrderHistoryMapper.updateDeliveryStatus(orderId, 9);  // ← 취소 상태
        log.info("주문 취소 - orderId: {}", orderId);
    }

    /* ─────────────────────────────────────────
       후기 작성
       ───────────────────────────────────────── */
    @Transactional
    public boolean writeReview(ReviewDTO dto) {
        if (customerReviewMapper.existsReview(dto.getOrderItemId())) {
            log.warn("이미 작성된 리뷰 - orderItemId: {}", dto.getOrderItemId());
            return false;
        }
        customerReviewMapper.insertReview(dto);
        log.info("리뷰 등록 - orderItemId: {}, customerId: {}, businessId: {}",
                dto.getOrderItemId(), dto.getCustomerId(), dto.getBusinessId());
        return true;
    }

    /* ─────────────────────────────────────────
       주문 삭제
       ───────────────────────────────────────── */
    @Transactional
    public void deleteOrder(Long orderId, Long customerId) {
        OrderHistoryDTO order = customerOrderHistoryMapper.selectOrderDetail(orderId);
        if (order == null) throw new IllegalArgumentException("존재하지 않는 주문입니다.");
        if (!order.getCustomerId().equals(customerId)) throw new IllegalArgumentException("본인 주문만 삭제할 수 있습니다.");
        customerOrderHistoryMapper.deleteOrderByOrderId(orderId);
        log.info("주문 삭제 - orderId: {}, customerId: {}", orderId, customerId);
    }
}