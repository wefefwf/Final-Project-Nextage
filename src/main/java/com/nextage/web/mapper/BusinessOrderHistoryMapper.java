package com.nextage.web.mapper;

import com.nextage.web.domain.OrderHistoryDTO;
import com.nextage.web.domain.OrderItemsDTO;
import com.nextage.web.domain.OrderSearchDTO;
import com.nextage.web.domain.ScheduleOrderDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BusinessOrderHistoryMapper {

    List<OrderHistoryDTO> selectOrdersByBusinessId(
            @Param("businessId") Long businessId,
            @Param("search")     OrderSearchDTO search,
            @Param("offset")     int offset,
            @Param("limit")      int limit);

    int countOrdersByBusinessId(
            @Param("businessId") Long businessId,
            @Param("search")     OrderSearchDTO search);

    // BADMIN 전체 조회
    List<OrderHistoryDTO> selectAllOrdersForAdmin(
            @Param("search") OrderSearchDTO search,
            @Param("offset") int offset,
            @Param("limit")  int limit);

    int countAllOrdersForAdmin(@Param("search") OrderSearchDTO search);

    OrderHistoryDTO     selectOrderDetail(@Param("orderId") Long orderId);
    List<OrderItemsDTO> selectOrderItems(@Param("orderId") Long orderId);
    void updateDeliveryStatus(@Param("orderId") Long orderId, @Param("deliveryStatus") int deliveryStatus);

    List<OrderHistoryDTO> selectPendingOrders(@Param("businessId") Long businessId);
    List<OrderHistoryDTO> selectPendingOrdersAll();

    List<OrderHistoryDTO> selectAcceptedOrders(
            @Param("businessId") Long businessId,
            @Param("search")     OrderSearchDTO search,
            @Param("offset")     int offset,
            @Param("limit")      int limit);

    int countAcceptedOrders(
            @Param("businessId") Long businessId,
            @Param("search")     OrderSearchDTO search);

    void updateAcceptStatus(@Param("orderId") Long orderId, @Param("acceptStatus") String acceptStatus);

    // 채팅방 생성
    void insertChatRoom(@Param("bidId")      Long bidId,
                        @Param("customerId") Long customerId,
                        @Param("businessId") Long businessId);

    // 생성된 채팅방 ID 조회
    Long selectRoomIdByBidId(@Param("bidId")      Long bidId,
                             @Param("customerId") Long customerId,
                             @Param("businessId") Long businessId);

    // chat_function 초기화
    void insertChatFunction(@Param("roomId")     Long roomId,
                            @Param("businessId") Long businessId,
                            @Param("customerId") Long customerId);
    
    List<ScheduleOrderDTO> selectScheduleOrders(@Param("businessId") Long businessId);
    
    Long selectRequestIdByBidId(@Param("bidId") Long bidId);
    
    int selectChatFunctionExists(@Param("roomId") Long roomId);
    
    int selectUnreadCount(@Param("roomId") Long roomId);
}