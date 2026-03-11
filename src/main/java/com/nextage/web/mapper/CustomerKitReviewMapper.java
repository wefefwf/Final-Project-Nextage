package com.nextage.web.mapper;

import com.nextage.web.domain.KitReviewDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerKitReviewMapper {
    void    insertReview(KitReviewDTO dto);
    boolean existsReview(@Param("orderItemId") Long orderItemId);
}