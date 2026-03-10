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
    public String getList(@RequestParam(value = "page", defaultValue = "1") int page, Model model) {
        int size = 9; // 한 페이지에 보여줄 상품 개수 (3줄 x 3열)
        
        // 1. 해당 페이지에 맞는 리스트만 가져오기 (offset 계산 필요)
        int offset = (page - 1) * size;
        List<KitDTO> kitList = shopService.getKitListPaged(offset, size);
        
        // 2. 전체 상품 개수를 가져와서 총 페이지 수 계산
        int totalCount = shopService.getTotalKitCount();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        model.addAttribute("kitList", kitList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        
        return "views/shop/customer-shop";
    }
    
    
    
    
    
    
    
    
    
    
}
