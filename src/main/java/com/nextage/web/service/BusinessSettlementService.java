package com.nextage.web.service;

import com.nextage.web.domain.SettlementDTO;
import com.nextage.web.mapper.BusinessSettlementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessSettlementService {

    private final BusinessSettlementMapper mapper;
    private static final int PAGE_SIZE = 10;

    @Transactional(readOnly = true)
    public List<SettlementDTO> getMySettlements(Long businessId, int page) {
        int offset = (page - 1) * PAGE_SIZE;
        return mapper.selectSettlementsByBusinessId(businessId, offset, PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public int getTotalPages(Long businessId, int page) {
        int total = mapper.countSettlementsByBusinessId(businessId);
        return (int) Math.ceil((double) total / PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public SettlementDTO getMonthSummary(Long businessId) {
        return mapper.selectMonthSummaryByBusinessId(businessId);
    }

    // BADMIN
    @Transactional(readOnly = true)
    public List<SettlementDTO> getBusinessSummaryList(int page) {
        int offset = (page - 1) * PAGE_SIZE;
        return mapper.selectBusinessSummaryList(offset, PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public int getBusinessSummaryTotalPages() {
        int total = mapper.countBusinessSummaryList();
        return (int) Math.ceil((double) total / PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public List<SettlementDTO> getSettlementsByBusinessId(Long businessId, int page) {
        int offset = (page - 1) * PAGE_SIZE;
        return mapper.selectSettlementsByBusinessIdForAdmin(businessId, offset, PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public int getSettlementsTotalPages(Long businessId) {
        int total = mapper.countSettlementsByBusinessIdForAdmin(businessId);
        return (int) Math.ceil((double) total / PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public SettlementDTO getMonthSummaryForAdmin(Long businessId) {
        return mapper.selectMonthSummaryByBusinessIdForAdmin(businessId);
    }
}