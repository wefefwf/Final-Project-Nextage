package com.nextage.web.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.nextage.web.domain.BizinfoDTO;
import com.nextage.web.domain.CareerDTO;
import com.nextage.web.domain.KitDTO;
import com.nextage.web.domain.ReviewDTO;
import com.nextage.web.mapper.BusinessPortfolioMapper;
import com.nextage.web.mapper.CustomerShopMapper;

@Service
public class BusinessPortfolioService {

	@Autowired
	public BusinessPortfolioMapper portfolioMapper;
	
	//업체정보 
	public BizinfoDTO getPortfolio(long id){
		return portfolioMapper.getPortfolio(id);
	}
	
	//리뷰 들고오기
	public List<ReviewDTO> getReview(long id,int size,int offset,boolean isMine){
		return portfolioMapper.getReview(id,size,offset,isMine);
	}
	
	//경력 들고오기
	public List<CareerDTO> getCareer(long id){
		return portfolioMapper.getCareer(id);
	}
	
	//리뷰 갯수들고오기 
		public long getTotalReviewCount(long id,boolean isMine){
			return portfolioMapper.getTotalReviewCount(id,isMine);
		}
		
	//status 변경
		public void updateStatus(long reviewId, String status){
			portfolioMapper.updateStatus(reviewId, status);
		}
	
}
