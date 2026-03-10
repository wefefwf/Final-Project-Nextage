package com.nextage.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nextage.web.domain.KitDTO;

@Mapper
public interface CustomerShopMapper {

	//리스트 갯수대로 가져오기
	public List<KitDTO> getKitListPaged(@Param("offset")int offset ,@Param("size")int size,@Param("sort")String sort);
	
	//총 갯수 가져오기
	public int getTotalKitCount();
	
	//게시글 하나 가져오기
	public KitDTO getDetail(int id);
	
	//게시글 하나 지우기
	public void deleteShop(int id);
}
