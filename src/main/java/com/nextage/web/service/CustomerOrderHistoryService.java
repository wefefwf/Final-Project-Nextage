package com.nextage.web.service;

import com.nextage.web.domain.KitReviewDTO;
import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.mapper.CustomerKitReviewMapper;
import com.nextage.web.mapper.CustomerOrderHistoryMapper;
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
    private final CustomerKitReviewMapper    customerKitReviewMapper;

    @Transactional(readOnly = true)
    public List<OrderHistoryDTO> getOrderHistory(Long customerId) {
        List<OrderHistoryDTO> orders =
            customerOrderHistoryMapper.selectOrdersByCustomerId(customerId);
        orders.forEach(order ->
            order.setItems(customerOrderHistoryMapper.selectOrderItems(order.getOrderId()))
        );
        return orders;
    }

    @Transactional(readOnly = true)
    public OrderHistoryDTO getOrderDetail(Long orderId) {
        OrderHistoryDTO order = customerOrderHistoryMapper.selectOrderDetail(orderId);
        order.setItems(customerOrderHistoryMapper.selectOrderItems(orderId));
        return order;
    }

    @Transactional
    public boolean writeReview(KitReviewDTO dto) {
        if (customerKitReviewMapper.existsReview(dto.getOrderItemId())) {
            return false;
        }
        customerKitReviewMapper.insertReview(dto);
        return true;
    }
}