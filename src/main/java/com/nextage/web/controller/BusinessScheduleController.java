package com.nextage.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.nextage.web.domain.BizinfoDTO;
import com.nextage.web.domain.BusinessDTO;
import com.nextage.web.domain.CareerDTO;
import com.nextage.web.domain.KitDTO;
import com.nextage.web.domain.ReviewDTO;
import com.nextage.web.domain.ScheduleOrderDTO;
import com.nextage.web.service.BusinessOrderHistoryService;
import com.nextage.web.service.BusinessPortfolioService;
import com.nextage.web.service.CustomerShopService;
import com.nextage.web.userDetails.BusinessUserDetails;

import jakarta.servlet.http.HttpSession;

@Controller
public class BusinessScheduleController {

	@Autowired
	public BusinessOrderHistoryService bohService;

	
	  //portfolio페이지 가기 -business
	  
	 @PreAuthorize("hasAnyRole('BUSER','BADMIN')") 
	 @GetMapping("/business/schedule")
	 public String goSchedule(@AuthenticationPrincipal BusinessUserDetails user,Model model){
		 
		 Long businessId = user.getBusiness().getBusinessId();
		 List<ScheduleOrderDTO> scheduleList = bohService.getScheduleOrders(businessId);
		 //정렬
		 scheduleList.sort((a, b) -> a.getDueDate().compareTo(b.getDueDate()));
		 
		 //오늘거 필터링
		 LocalDate today = LocalDate.now();
		 List<ScheduleOrderDTO> todayWork = scheduleList.stream()
				    .filter(order -> 
				        !order.getCreatedAt().toLocalDate().isAfter(today) &&
				        !order.getDueDate().toLocalDate().isBefore(today)
				    )
				    .collect(Collectors.toList());
		     // 4. 모델에 담아서 뷰(HTML)로 보내기
		     model.addAttribute("scheduleList", scheduleList); // 전체 일정 리스트 & 달력용
		     model.addAttribute("todayWork", todayWork);   // 오늘 작업 섹션용
		     model.addAttribute("companyName", user.getBusiness().getCompanyName());

		     return "views/schedule/business-schedule"; // 실제 HTML 파일 경로 (사장님 설정에 맞게)
		 }

	 
	 @GetMapping("/business/schedule/active")
	 @ResponseBody
	 public ResponseEntity<List<ScheduleOrderDTO>> getActiveSchedule(
	         @AuthenticationPrincipal BusinessUserDetails user,
	         @RequestParam(name = "dueDate", required = false) String dueDateStr) {

	     if (user == null) return ResponseEntity.status(401).build();

	     Long businessId = user.getBusiness().getBusinessId();
	     List<ScheduleOrderDTO> scheduleList = bohService.getScheduleOrders(businessId);

	     if (dueDateStr == null) return ResponseEntity.ok(List.of());

	     LocalDate targetDueDate = LocalDate.parse(dueDateStr);

	     List<ScheduleOrderDTO> activeList = scheduleList.stream()
	             .filter(order -> {
	                 LocalDate existingDueDate = order.getDueDate().toLocalDate();
	                 long diff = java.time.temporal.ChronoUnit.DAYS
	                         .between(existingDueDate, targetDueDate);
	                 return Math.abs(diff) <= 2; // ← 새 주문 마감일 기준 앞뒤 2일
	             })
	             .collect(Collectors.toList());

	     return ResponseEntity.ok(activeList);
	 }


}
