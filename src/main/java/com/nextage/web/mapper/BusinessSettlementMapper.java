package com.nextage.web.mapper;

import com.nextage.web.domain.SettlementDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BusinessSettlementMapper {

    // BUSER: 본인 정산 내역
    List<SettlementDTO> selectSettlementsByBusinessId(
            @Param("businessId") Long businessId,
            @Param("offset") int offset,
            @Param("limit")  int limit);

    int countSettlementsByBusinessId(@Param("businessId") Long businessId);

    // BUSER: 이번 달 요약
    SettlementDTO selectMonthSummaryByBusinessId(@Param("businessId") Long businessId);

    // BADMIN: 업체 목록 (업체별 정산 합계)
    List<SettlementDTO> selectBusinessSummaryList(
            @Param("offset") int offset,
            @Param("limit")  int limit);

    int countBusinessSummaryList();

    // BADMIN: 특정 업체 정산 상세
    List<SettlementDTO> selectSettlementsByBusinessIdForAdmin(
            @Param("businessId") Long businessId,
            @Param("offset") int offset,
            @Param("limit")  int limit);

    int countSettlementsByBusinessIdForAdmin(@Param("businessId") Long businessId);

    // BADMIN: 특정 업체 이번 달 요약
    SettlementDTO selectMonthSummaryByBusinessIdForAdmin(@Param("businessId") Long businessId);
}