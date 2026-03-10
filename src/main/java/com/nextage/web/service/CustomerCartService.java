package com.nextage.web.service;

import com.nextage.web.domain.CartDTO;
import com.nextage.web.mapper.CustomerCartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerCartService {

    private final CustomerCartMapper customerCartMapper;

    // 1. 장바구니 아이템 목록 조회
    @Transactional(readOnly = true)
    public List<CartDTO> getCartItems(Long customerId) {
        Long cartId = customerCartMapper.selectCartIdByCustomerId(customerId);
        if (cartId == null) return List.of();
        return customerCartMapper.selectCartItems(cartId);
    }

    // 2. 장바구니 상품 추가 (동일 kit이면 수량 누적)
    @Transactional
    public void addToCart(CartDTO dto, Long customerId) {
        Long cartId = getOrCreateCartId(customerId);

        Long duplicateId = customerCartMapper.selectDuplicateCartItemId(
                cartId, dto.getProductId());

        if (duplicateId != null) {
            customerCartMapper.updateCartItemQuantityAdd(duplicateId, dto.getQuantity());
        } else {
            customerCartMapper.insertCartItem(cartId, dto.getProductId(), dto.getQuantity());
        }
    }

    // 3. 수량 변경
    @Transactional
    public void updateQuantity(CartDTO dto) {
        if (dto.getQuantity() < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        customerCartMapper.updateCartItemQuantity(dto.getCartItemId(), dto.getQuantity());
    }

    // 4. 단건 삭제
    @Transactional
    public void deleteCartItem(Long cartItemId) {
        customerCartMapper.deleteCartItem(cartItemId);
    }

    // 5. 선택 삭제
    @Transactional
    public void deleteSelected(CartDTO dto, Long customerId) {
        Long cartId = getOrCreateCartId(customerId);

        List<Long> ids = dto.getIds().stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());

        customerCartMapper.deleteCartItemsByIds(ids, cartId);
    }

    // Private: 장바구니 ID 조회 or 신규 생성
    private Long getOrCreateCartId(Long customerId) {
        Long cartId = customerCartMapper.selectCartIdByCustomerId(customerId);
        if (cartId == null) {
            customerCartMapper.insertCart(customerId);
            cartId = customerCartMapper.selectLastInsertCartId(customerId);
        }
        return cartId;
    }
}