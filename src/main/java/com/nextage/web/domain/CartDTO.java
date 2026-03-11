package com.nextage.web.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    // ── 조회 응답용 ──────────────────────────────────
    private Long    cartItemId;
    private Long    kitId;
    private String  productName;
    private String  imageUrl;
    private int     price;
    private int     quantity;
    private String  kitStatus;     // ✅ 추가 — ACTIVE / SOLDOUT

    // ── 추가 / 수정 요청용 ───────────────────────────
    // ── 선택 삭제 요청용 ─────────────────────────────
    private List<String> ids;

    // ── 결제 요청용 ──────────────────────────────────
    private List<String> cartItemIds;

    // ── 편의 메서드 ──────────────────────────────────
    public boolean isSoldOut() {
        return "SOLDOUT".equals(kitStatus);
    }
}