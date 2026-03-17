package com.nextage.web.mapper;

import com.nextage.web.domain.ReviewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerReviewMapper {

	public ReviewDTO getOrderItemForReview(@Param("orderItemId") long orderItemId,@Param("loginId")String loginId);
}