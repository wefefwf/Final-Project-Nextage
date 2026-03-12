package com.nextage.web.service;

import com.nextage.web.domain.OrderHistoryDTO;
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
    public List<OrderHistoryDTO> getOrderHistory(Long customerId, int page) {

        int offset = (page - 1) * PAGE_SIZE;

        List<OrderHistoryDTO> orders =
            customerOrderHistoryMapper.selectOrdersByCustomerId(customerId, offset, PAGE_SIZE);

        orders.forEach(order ->
            order.setItems(customerOrderHistoryMapper.selectOrderItems(order.getOrderId()))
        );

        return orders;
    }

    @Transactional(readOnly = true)
    public int getTotalPages(Long customerId) {
        int total = customerOrderHistoryMapper.countOrdersByCustomerId(customerId);
        return (int) Math.ceil((double) total / PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public OrderHistoryDTO getOrderDetail(Long orderId) {
        OrderHistoryDTO order = customerOrderHistoryMapper.selectOrderDetail(orderId);
        order.setItems(customerOrderHistoryMapper.selectOrderItems(orderId));
        return order;
    }

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

    @Transactional
    public void deleteOrder(Long orderId, Long customerId) {
        OrderHistoryDTO order = customerOrderHistoryMapper.selectOrderDetail(orderId);
        if (order == null) {
            throw new IllegalArgumentException("존재하지 않는 주문입니다.");
        }
        // FK 순서 준수: review → order_items → business_settlement → orders
        customerOrderHistoryMapper.deleteReviewsByOrderId(orderId);
        customerOrderHistoryMapper.deleteOrderItemsByOrderId(orderId);
        customerOrderHistoryMapper.deleteSettlementsByOrderId(orderId);  // 추가
        customerOrderHistoryMapper.deleteOrderByOrderId(orderId);
        log.info("주문 삭제 - orderId: {}, customerId: {}", orderId, customerId);
    }
}