package com.nextage.web.service;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.OrderSearchDTO;
import com.nextage.web.domain.ScheduleOrderDTO;
import com.nextage.web.mapper.BusinessOrderHistoryMapper;
import com.nextage.web.mapper.CustomerRequestMapper;

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
    private final CustomerRequestMapper requestMapper;

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

    // 수정
    @Transactional
    public void acceptOrder(Long orderId) {
        mapper.updateAcceptStatus(orderId, "ACCEPTED");
        log.info("주문 수락 - orderId: {}", orderId);

        OrderHistoryDTO order = mapper.selectOrderDetail(orderId);

        if (order.getBidId() != null) {
            // request status → SELECTED
            Long requestId = mapper.selectRequestIdByBidId(order.getBidId());
            requestMapper.updateStatus(requestId, "SELECTED");

            if (order.getRoomId() == null) {
                mapper.insertChatRoom(
                    order.getBidId(),
                    order.getCustomerId(),
                    order.getBusinessId()
                );
                Long roomId = mapper.selectRoomIdByBidId(
                    order.getBidId(),
                    order.getCustomerId(),
                    order.getBusinessId()
                );
                mapper.insertChatFunction(roomId, order.getBusinessId(), order.getCustomerId());
                log.info("채팅방 생성 - roomId: {}, bidId: {}", roomId, order.getBidId());
            }
        }
    }

    @Transactional
    public void rejectOrder(Long orderId) {
        mapper.updateAcceptStatus(orderId, "REJECTED");
        log.info("주문 거절 - orderId: {}", orderId);

        OrderHistoryDTO order = mapper.selectOrderDetail(orderId);
        if (order.getBidId() != null) {
            // request status → OPEN으로 복구
            Long requestId = mapper.selectRequestIdByBidId(order.getBidId());
            requestMapper.updateStatus(requestId, "OPEN");
        }
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