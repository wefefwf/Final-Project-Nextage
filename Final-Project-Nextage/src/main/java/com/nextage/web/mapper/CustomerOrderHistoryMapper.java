package com.nextage.web.mapper;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.OrderItemsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CustomerOrderHistoryMapper {
    List<OrderHistoryDTO> selectOrdersByCustomerId(@Param("customerId") Long customerId,
                                                   @Param("offset")     int offset,
                                                   @Param("limit")      int limit);
    int                   countOrdersByCustomerId(@Param("customerId") Long customerId);
    OrderHistoryDTO        selectOrderDetail(@Param("orderId") Long orderId);
    List<OrderItemsDTO>     selectOrderItems(@Param("orderId") Long orderId);
    void                   insertOrderItems(OrderItemsDTO dto);

    // 삭제 관련
    void deleteReviewsByOrderId(@Param("orderId") Long orderId);
    void deleteOrderItemsByOrderId(@Param("orderId") Long orderId);
    void deleteOrderByOrderId(@Param("orderId") Long orderId);
    void deleteSettlementsByOrderId(@Param("orderId") Long orderId);
}