package com.nextage.web.mapper;

import com.nextage.web.domain.ReviewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerReviewMapper {
    void insertReview(ReviewDTO dto);
    boolean existsReview(@Param("orderItemId") Long orderItemId);
    ReviewDTO selectReviewByOrderItemId(@Param("orderItemId") Long orderItemId); // 추가
}