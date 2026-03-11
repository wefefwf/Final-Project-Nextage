package com.nextage.web.mapper;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.OrderItemDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CustomerOrderHistoryMapper {
    List<OrderHistoryDTO> selectOrdersByCustomerId(@Param("customerId") Long customerId);
    OrderHistoryDTO        selectOrderDetail(@Param("orderId") Long orderId);
    List<OrderItemDTO>     selectOrderItems(@Param("orderId") Long orderId);
    void                   insertOrderItem(OrderItemDTO dto);
}