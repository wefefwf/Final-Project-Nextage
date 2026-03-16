package com.nextage.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nextage.web.domain.BizinfoDTO;
import com.nextage.web.domain.CareerDTO;
import com.nextage.web.domain.KitDTO;
import com.nextage.web.domain.ReviewDTO;

@Mapper
public interface BusinessPortfolioMapper {

	//업체정보 
	public BizinfoDTO getPortfolio(@Param("id") long id);
	
	//리뷰 들고오기
	public List<ReviewDTO> getReview(@Param("id") long id,@Param("size") int size,@Param("offset") int offset);
	
	//경력 들고오기
	public List<CareerDTO> getCareer(@Param("id") long id);
	
	//리뷰갯수들고오기
	public long getTotalReviewCount(@Param("id") long id);
}
