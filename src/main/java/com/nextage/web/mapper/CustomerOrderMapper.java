package com.nextage.web.mapper;

import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerOrderMapper {

    // ── 기존 메서드 (변경 없음) ──────────────────────────────────

    void insertOrder(@Param("orderNo")     String        orderNo,
                     @Param("customerId")  Long          customerId,
                     @Param("businessId")  Long          businessId,
                     @Param("totalAmount") int           totalAmount,
                     @Param("dueDate")     LocalDateTime dueDate);

    void updatePaymentStatus(@Param("orderNo")       String orderNo,
                             @Param("impUid")        String impUid,
                             @Param("paymentStatus") String paymentStatus);

    Integer selectTotalAmountByOrderNo(@Param("orderNo") String orderNo);

    Long selectOrderIdByOrderNo(@Param("orderNo") String orderNo);

    // ── 신규: 취소/거절용 ────────────────────────────────────────

    /** 현재 결제 상태 조회 (READY / PAID / CANCELLED / REJECTED / FAILED) */
    String selectPaymentStatusByOrderNo(@Param("orderNo") String orderNo);

    /** 아임포트 취소 API 호출에 필요한 imp_uid 조회 */
    String selectImpUidByOrderNo(@Param("orderNo") String orderNo);

    /** 취소/거절 시 payment_status + accept_status 업데이트 */
    void updateCancelStatus(@Param("orderNo")       String orderNo,
                            @Param("paymentStatus") String paymentStatus);
}