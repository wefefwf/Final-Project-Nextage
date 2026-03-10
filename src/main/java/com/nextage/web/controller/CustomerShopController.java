package com.nextage.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nextage.web.domain.KitDTO;
import com.nextage.web.service.CustomerShopService;

@Controller
public class CustomerShopController {

    private final CustomerMainController customerMainController;

	@Autowired
	public CustomerShopService shopService;

    CustomerShopController(CustomerMainController customerMainController) {
        this.customerMainController = customerMainController;
    }
	
	 // 샵 가기 페이지
	//리스트 뽑아가기
    @GetMapping("/customer/shop")
    public String getList(
        @RequestParam(value = "page", defaultValue = "1") int page, 
        @RequestParam(value = "sort", defaultValue = "price_desc") String sort, // 정렬 기준 추가
        Model model) {
        
        int size = 9;
        int offset = (page - 1) * size;

        // 정렬 기준을 서비스에 넘겨서 쿼리에서 처리하게 합니다.
        List<KitDTO> kitList = shopService.getKitListPaged(offset, size, sort);
        int totalCount = shopService.getTotalKitCount();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        model.addAttribute("kitList", kitList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentSort", sort); // 현재 어떤 정렬인지 뷰에 전달
        
        return "views/shop/customer-shop";
    }
    
    
    
    
    
    
    
    
    
    
}
