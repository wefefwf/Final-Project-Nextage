package com.nextage.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.nextage.web.domain.KitDTO;
import com.nextage.web.service.CustomerShopService;

@Controller
public class CustomerShopController {

	@Autowired
	public CustomerShopService shopService;

	 //샵 가기-페이지
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
        long totalCount = shopService.getTotalKitCount();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        model.addAttribute("kitList", kitList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentSort", sort); // 현재 어떤 정렬인지 뷰에 전달
        
        return "views/shop/customer-shop";
    }
    
    //샵 제품  추가 폼가기
    @GetMapping("/customer/shopForm")
    public String goShopInsertForm(){
    	return "views/shop/customer-shopInsertForm";
    }
    
    
    //샵 추가 로직 post
    @PostMapping("/customer/shopForm")
    public String insertKit(KitDTO kitDto, 
    		@RequestParam("mainImageFile1") MultipartFile file1, 
            @RequestParam("mainImageFile2") MultipartFile file2, 
            @RequestParam("detailImageFile") MultipartFile detailFile){
    	
    	shopService.addKit(kitDto, file1, file2, detailFile);
    	
    	return "redirect:/customer/shop";
    }
    
    
    
    //샵 상세페이지가기
    @GetMapping("customer/shop/detail")
    public String goShopDetail(@RequestParam("id") long id,@RequestParam(value = "page", defaultValue = "1") int page,Model model){
    	KitDTO kit = shopService.getDetail(id);
    	model.addAttribute("kit",kit);
    	model.addAttribute("page", page);
    	return "views/shop/customer-shopDetail";
    }
    
    //제품 삭제
    @GetMapping("/customer/shop/delete")
    public String deleteShop(@RequestParam("id") long id){
    	shopService.deleteShop(id);
    	return "redirect:/customer/shop";
    }
    
    //업데이트 폼 가기
    @GetMapping("customer/shop/edit")
    public String goShopUpdate(@RequestParam("id") long id,@RequestParam(value = "page", defaultValue = "1") int page,Model model){
    	KitDTO kit = shopService.getDetail(id);
    	model.addAttribute("kit",kit);
    	model.addAttribute("page", page);
    	return "views/shop/customer-shopUpdateForm";
    }
    
    //업데이트 로직
    @PostMapping("/customer/shop/edit")
    public String update(KitDTO kitDto, 
    		@RequestParam("mainImageFile1") MultipartFile file1, 
            @RequestParam("mainImageFile2") MultipartFile file2, 
            @RequestParam("detailImageFile") MultipartFile detailFile,
            @RequestParam(value = "page", defaultValue = "1") int page
                         ) {
        
    	shopService.updateKit(kitDto, file1, file2, detailFile);
    	
        return "redirect:/customer/shop?page="+page;
    }
}
