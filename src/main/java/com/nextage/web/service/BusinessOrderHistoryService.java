package com.nextage.web.service;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.OrderSearchDTO;
import com.nextage.web.domain.ScheduleOrderDTO;
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

    @Transactional(readOnly = true)
    public List<OrderHistoryDTO> getPendingOrders(Long businessId, String role) {
        List<OrderHistoryDTO> orders;
        if ("BADMIN".equals(role)) {
            orders = mapper.selectPendingOrdersAll();
        } else {
            orders = mapper.selectPendingOrders(businessId);
        }
        orders.forEach(o -> o.setItems(mapper.selectOrderItems(o.getOrderId())));
        return orders;
    }

    @Transactional(readOnly = true)
    public List<OrderHistoryDTO> getAcceptedOrders(Long businessId, String role, OrderSearchDTO search, int page) {
        int offset = (page - 1) * PAGE_SIZE;
        List<OrderHistoryDTO> orders;
        if ("BADMIN".equals(role)) {
            orders = mapper.selectAllOrdersForAdmin(search, offset, PAGE_SIZE);
        } else {
            orders = mapper.selectAcceptedOrders(businessId, search, offset, PAGE_SIZE);
        }
        orders.forEach(o -> o.setItems(mapper.selectOrderItems(o.getOrderId())));
        return orders;
    }

    @Transactional(readOnly = true)
    public int getTotalPages(Long businessId, String role, OrderSearchDTO search) {
        int total;
        if ("BADMIN".equals(role)) {
            total = mapper.countAllOrdersForAdmin(search);
        } else {
            total = mapper.countAcceptedOrders(businessId, search);
        }
        return (int) Math.ceil((double) total / PAGE_SIZE);
    }

    @Transactional
    public void acceptOrder(Long orderId) {
        // 1. 수락 상태 변경
        mapper.updateAcceptStatus(orderId, "ACCEPTED");
        log.info("주문 수락 - orderId: {}", orderId);

        // 2. 주문 정보 조회 (bid_id, customer_id, business_id 필요)
        OrderHistoryDTO order = mapper.selectOrderDetail(orderId);

        // 3. 채팅방이 없을 때만 생성
        if (order.getBidId() != null && order.getRoomId() == null) {
            mapper.insertChatRoom(
                order.getBidId(),
                order.getCustomerId(),
                order.getBusinessId()
            );
            // 4. 생성된 채팅방 ID 조회
            Long roomId = mapper.selectRoomIdByBidId(
                order.getBidId(),
                order.getCustomerId(),
                order.getBusinessId()
            );
            // 5. chat_function 초기화
            mapper.insertChatFunction(roomId, order.getBusinessId(), order.getCustomerId());
            log.info("채팅방 생성 - roomId: {}, bidId: {}", roomId, order.getBidId());
        }
    }

    @Transactional
    public void rejectOrder(Long orderId) {
        mapper.updateAcceptStatus(orderId, "REJECTED");
        log.info("주문 거절 - orderId: {}", orderId);
    }

    @Transactional
    public void updateDeliveryStatus(Long orderId, int deliveryStatus) {
        mapper.updateDeliveryStatus(orderId, deliveryStatus);
        log.info("배송 상태 변경 - orderId: {}, status: {}", orderId, deliveryStatus);
    }

    @Transactional(readOnly = true)
    public OrderHistoryDTO getOrderDetail(Long orderId) {
        OrderHistoryDTO order = mapper.selectOrderDetail(orderId);
        if (order != null) {
            order.setItems(mapper.selectOrderItems(orderId));
        }
        return order;
    }
    
    @Transactional(readOnly = true) 
    public List<ScheduleOrderDTO> getScheduleOrders(Long businessId) {
    List<ScheduleOrderDTO> orders = mapper.selectScheduleOrders(businessId);
     return orders; 
     
    }
}