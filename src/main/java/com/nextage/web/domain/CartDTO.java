package com.nextage.web.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 장바구니 통합 DTO
 *
 * [조회 응답]  cartItemId, productName, imageUrl, optionSummary, price, quantity, isNew, isBest
 * [추가 요청]  productId, optionId, quantity
 * [수정 요청]  cartItemId, quantity
 * [선택삭제]   ids
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    // ── 조회 응답용 ──────────────────────────────────
    private Long    cartItemId;
    private String  productName;
    private String  imageUrl;
    private String  optionSummary;
    private int     price;
    private int     quantity;
    private boolean isNew;
    private boolean isBest;

    // ── 추가 / 수정 요청용 ───────────────────────────
    private Long productId;
    private Long optionId;

    // ── 선택 삭제 요청용 ─────────────────────────────
    private List<String> ids;
}