package com.nextage.web.service;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.OrderSearchDTO;
import com.nextage.web.domain.ReviewDTO;
import com.nextage.web.domain.ScheduleOrderDTO;
import com.nextage.web.mapper.BusinessOrderHistoryMapper;
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
    private final BusinessOrderHistoryMapper businessOrderHistoryMapper;
    private final CustomerPaymentService     paymentService;
    // ✅ BidService 제거 - Kit 구매만 다루므로 불필요
    private static final int PAGE_SIZE = 5;

    @Transactional(readOnly = true)
    public List<OrderHistoryDTO> getOrderHistory(Long customerId, String role, OrderSearchDTO search, int page) {
        int offset = (page - 1) * PAGE_SIZE;
        List<OrderHistoryDTO> orders;
        if ("CADMIN".equals(role)) {
            orders = customerOrderHistoryMapper.selectOrdersForAdmin(search, offset, PAGE_SIZE);
        } else {
            orders = customerOrderHistoryMapper.selectOrdersByCustomerId(customerId, search, offset, PAGE_SIZE);
        }
        orders.forEach(order -> {
            order.setItems(customerOrderHistoryMapper.selectOrderItems(order.getOrderId()));
            if (order.getBidId() != null && order.getAcceptStatus() != null
                    && !"REJECTED".equals(order.getAcceptStatus())) {
                order.setUnreadCount(
                    customerOrderHistoryMapper.selectUnreadCountByOrderId(order.getOrderId())
                );
            }
        });
        return orders;
    }

    @Transactional(readOnly = true)
    public int getTotalPages(Long customerId, String role, OrderSearchDTO search) {
        int total;
        if ("CADMIN".equals(role)) {
            total = customerOrderHistoryMapper.countOrdersForAdmin(search);
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
        // ✅ Kit 구매만 다루므로 COMPLETE 로직 불필요
        customerOrderHistoryMapper.updateDeliveryStatus(orderId, deliveryStatus);
        log.info("배송 상태 변경 - orderId: {}, status: {}", orderId, deliveryStatus);
    }

    @Transactional
    public void acceptOrder(Long orderId) {
        customerOrderHistoryMapper.updateAcceptStatus(orderId, "ACCEPTED");
        updateDeliveryStatus(orderId, 2);
        log.info("주문 수락 - orderId: {}", orderId);
    }

    @Transactional
    public void rejectOrder(Long orderId) {
        customerOrderHistoryMapper.updateAcceptStatus(orderId, "REJECTED");
        customerOrderHistoryMapper.updateDeliveryStatus(orderId, 9);

        // ✅ order_no 조회 후 포트원 취소 API + payment_status 업데이트
        OrderHistoryDTO order = customerOrderHistoryMapper.selectOrderDetail(orderId);
        if (order != null && order.getOrderNo() != null) {
            paymentService.cancelPayment(order.getOrderNo(), "관리자 거절");
        }

        log.info("주문 취소 - orderId: {}", orderId);
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

    @Transactional
    public Long getOrCreateChatRoom(Long orderId, Long customerId) {
        OrderHistoryDTO order = customerOrderHistoryMapper.selectOrderDetail(orderId);

        if (order.getRoomId() != null) return order.getRoomId();

        if (order.getBidId() == null) throw new IllegalArgumentException("개인거래 주문이 아닙니다.");

        businessOrderHistoryMapper.insertChatRoom(
            order.getBidId(),
            customerId,
            order.getBusinessId()
        );
        Long roomId = businessOrderHistoryMapper.selectRoomIdByBidId(
            order.getBidId(),
            customerId,
            order.getBusinessId()
        );

        int exists = businessOrderHistoryMapper.selectChatFunctionExists(roomId);
        if (exists == 0) {
            businessOrderHistoryMapper.insertChatFunction(roomId, order.getBusinessId(), customerId);
        }

        return roomId;
    }
}