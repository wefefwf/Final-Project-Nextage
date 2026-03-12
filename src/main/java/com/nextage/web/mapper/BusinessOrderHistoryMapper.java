package com.nextage.web.mapper;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.OrderItemsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BusinessOrderHistoryMapper {

    // 기존
    List<OrderHistoryDTO> selectOrdersByBusinessId(
            @Param("businessId") Long businessId,
            @Param("offset")     int  offset,
            @Param("limit")      int  limit);

    int countOrdersByBusinessId(@Param("businessId") Long businessId);

    OrderHistoryDTO selectOrderDetail(@Param("orderId") Long orderId);

    List<OrderItemsDTO> selectOrderItems(@Param("orderId") Long orderId);

    void updateDeliveryStatus(@Param("orderId")        Long orderId,
                              @Param("deliveryStatus") int  deliveryStatus);

    // 추가
    List<OrderHistoryDTO> selectPendingOrders(@Param("businessId") Long businessId);

    List<OrderHistoryDTO> selectAcceptedOrders(
            @Param("businessId") Long businessId,
            @Param("offset")     int  offset,
            @Param("limit")      int  limit);

    int countAcceptedOrders(@Param("businessId") Long businessId);

    void updateAcceptStatus(@Param("orderId")      Long   orderId,
                            @Param("acceptStatus") String acceptStatus);
}