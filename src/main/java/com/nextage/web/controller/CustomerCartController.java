package com.nextage.web.controller;

import com.nextage.web.domain.CartDTO;
import com.nextage.web.service.CustomerCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CustomerCartController {

    private final CustomerCartService customerCartService;

    // 임시 테스트용 ID (로그인 기능 완성 후 제거)
    private static final Long TEMP_CUSTOMER_ID = 1L;

    private Long getCustomerId(UserDetails userDetails) {
        // TODO: 로그인 완성 후 userDetails에서 customer_id(Long) 추출로 교체
        return TEMP_CUSTOMER_ID;
    }

    // GET /cart
    @GetMapping
    public String cartPage(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        List<CartDTO> cartItems = customerCartService.getCartItems(getCustomerId(userDetails));
        model.addAttribute("cartItems", cartItems);
        return "views/shop/customer-cart";
    }

    // PUT /cart/update
    @PutMapping("/update")
    @ResponseBody
    public ResponseEntity<Void> updateQuantity(@RequestBody CartDTO dto) {
        customerCartService.updateQuantity(dto);
        return ResponseEntity.ok().build();
    }

    // DELETE /cart/delete/{cartItemId}  — 단건 삭제
    @DeleteMapping("/delete/{cartItemId}")
    @ResponseBody
    public ResponseEntity<Void> deleteItem(
            @PathVariable("cartItemId") Long cartItemId) {
        customerCartService.deleteCartItem(cartItemId);
        return ResponseEntity.ok().build();
    }

    // DELETE /cart/delete-selected  — 선택 삭제 ✅ URL 변경
    @DeleteMapping("/delete-selected")
    @ResponseBody
    public ResponseEntity<Void> deleteSelected(
            @RequestBody CartDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        customerCartService.deleteSelected(dto, getCustomerId(userDetails));
        return ResponseEntity.ok().build();
    }

    // POST /cart/add
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<String> addToCart(
            @RequestBody CartDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        customerCartService.addToCart(dto, getCustomerId(userDetails));
        return ResponseEntity.ok("장바구니에 추가되었습니다.");
    }
}