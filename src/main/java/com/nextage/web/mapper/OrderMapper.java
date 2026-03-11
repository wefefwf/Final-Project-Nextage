package com.nextage.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {

    // 주문 사전 생성 (payment_status = READY)
    void insertOrder(@Param("orderNo")      String orderNo,
                     @Param("customerId")   Long   customerId,
                     @Param("totalAmount")  int    totalAmount);

    // 결제 완료 후 imp_uid + payment_status 업데이트
    void updatePaymentStatus(@Param("orderNo")       String orderNo,
                              @Param("impUid")        String impUid,
                              @Param("paymentStatus") String paymentStatus);

    // order_no로 total_amount 조회 (금액 검증용)
    Integer selectTotalAmountByOrderNo(@Param("orderNo") String orderNo);
}