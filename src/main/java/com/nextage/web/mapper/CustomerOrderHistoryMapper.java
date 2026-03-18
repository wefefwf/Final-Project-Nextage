package com.nextage.web.mapper;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.OrderItemsDTO;
import com.nextage.web.domain.OrderSearchDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerOrderHistoryMapper {

    // CUSER: 본인 Kit 주문 목록 (bid_id IS NULL)
    List<OrderHistoryDTO> selectOrdersByCustomerId(
            @Param("customerId") Long customerId,
            @Param("search")     OrderSearchDTO search,
            @Param("offset")     int offset,
            @Param("limit")      int limit);

    int countOrdersByCustomerId(
            @Param("customerId") Long customerId,
            @Param("search")     OrderSearchDTO search);

    // CADMIN: 전체 Kit 주문 목록 (bid_id IS NULL)
    List<OrderHistoryDTO> selectAllOrders(
            @Param("search") OrderSearchDTO search,
            @Param("offset") int offset,
            @Param("limit")  int limit);

    int countAllOrders(@Param("search") OrderSearchDTO search);

    // 공통
    OrderHistoryDTO     selectOrderDetail(@Param("orderId") Long orderId);
    List<OrderItemsDTO> selectOrderItems(@Param("orderId") Long orderId);

    // CADMIN: PENDING Kit 주문 (새로 들어온 주문)
    List<OrderHistoryDTO> selectPendingOrders();

    // CADMIN 전용 상태 변경
    void updateDeliveryStatus(@Param("orderId") Long orderId,
                              @Param("deliveryStatus") int deliveryStatus);

    void updateAcceptStatus(@Param("orderId") Long orderId,
                            @Param("acceptStatus") String acceptStatus);

    // CUSER 주문 삭제
    // CUSER 주문 아이템 저장
    void insertOrderItems(OrderItemsDTO item);

    void deleteReviewsByOrderId(@Param("orderId") Long orderId);
    void deleteOrderItemsByOrderId(@Param("orderId") Long orderId);
    void deleteOrderByOrderId(@Param("orderId") Long orderId);
    void deleteSettlementsByOrderId(@Param("orderId") Long orderId);
    int selectUnreadCountByOrderId(@Param("orderId") Long orderId);
    
    List<OrderHistoryDTO> selectOrdersForAdmin(
            @Param("search") OrderSearchDTO search,
            @Param("offset") int offset,
            @Param("limit") int limit);

    int countOrdersForAdmin(@Param("search") OrderSearchDTO search);
}