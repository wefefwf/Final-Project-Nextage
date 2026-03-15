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
	public List<ReviewDTO> getReview(@Param("id") long id,@Param("size") int size,
														@Param("offset") int offset,@Param("isMine") boolean isMine);
	
	//경력 들고오기
	public List<CareerDTO> getCareer(@Param("id") long id);
	
	//리뷰갯수들고오기
	public long getTotalReviewCount(@Param("id") long id,@Param("isMine") boolean isMine);
	
	//모달 status 변경
	public void updateStatus(@Param("reviewId") long reviewId, @Param("status") String status);
	
	//위치변경
	public void updateLocation(@Param("businessId") long businessId,@Param("location") String location);
	
	//위치삭제
	public void deleteLocation(@Param("businessId") long businessId);
		
	// 프로필 삭제
	public void deleteProfileImage(@Param("id") long id);	
	
	//경력 삭제
	public void deleteCareer(@Param("careerId") long careerId);
	
	//경력 업데이트
	public void updateCareer(@Param("careerId") long careerId,@Param("workDescription") String workDescription);
	
	//경력추가
	public void addCareer(@Param("businessId")long businessId,@Param("workDescription") String workDescription);
}
