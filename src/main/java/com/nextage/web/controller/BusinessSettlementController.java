package com.nextage.web.controller;

import com.nextage.web.domain.SettlementDTO;
import com.nextage.web.service.BusinessSettlementService;
import com.nextage.web.userDetails.BusinessUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/business/settlement")
@RequiredArgsConstructor
public class BusinessSettlementController {

    private final BusinessSettlementService service;

    // BUSER: 본인 정산 내역
    @GetMapping
    public String settlementPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @AuthenticationPrincipal BusinessUserDetails userDetails,
            Model model) {

        if (userDetails == null) return "redirect:/business/login";

        String role       = userDetails.getRole();
        Long   businessId = userDetails.getBusinessId();

        if ("BADMIN".equals(role)) {
            // BADMIN: 업체 목록
            List<SettlementDTO> list = service.getBusinessSummaryList(page);
            int totalPages = service.getBusinessSummaryTotalPages();
            model.addAttribute("summaryList", list);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages",  totalPages);
            model.addAttribute("isAdmin",     true);
            return "views/settlement/business-settlement-admin";
        }

        // BUSER: 본인 정산 내역
        List<SettlementDTO> settlements = service.getMySettlements(businessId, page);
        SettlementDTO       summary     = service.getMonthSummary(businessId);
        int totalPages = service.getTotalPages(businessId, page);

        model.addAttribute("settlements", settlements);
        model.addAttribute("summary",     summary);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  totalPages);
        model.addAttribute("isAdmin",     false);
        return "views/settlement/business-settlement";
    }

    // BADMIN: 특정 업체 상세
    @GetMapping("/detail/{businessId}")
    public String detailPage(
            @PathVariable("businessId") Long businessId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @AuthenticationPrincipal BusinessUserDetails userDetails,
            Model model) {

        if (userDetails == null) return "redirect:/business/login";
        if (!"BADMIN".equals(userDetails.getRole()))
            return "redirect:/business/settlement";

        List<SettlementDTO> settlements = service.getSettlementsByBusinessId(businessId, page);
        SettlementDTO       summary     = service.getMonthSummaryForAdmin(businessId);
        int totalPages = service.getSettlementsTotalPages(businessId);

        model.addAttribute("settlements", settlements);
        model.addAttribute("summary",     summary);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages",  totalPages);
        model.addAttribute("targetBusinessId", businessId);

        // 업체명 표시용
        if (!settlements.isEmpty()) {
            model.addAttribute("companyName", settlements.get(0).getCompanyName());
        }

        return "views/settlement/business-settlement-detail";
    }
}