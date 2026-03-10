package com.nextage.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nextage.web.domain.KitDTO;
import com.nextage.web.mapper.CustomerShopMapper;

@Service
public class CustomerShopService {

	@Autowired
	public CustomerShopMapper shopMapper;
	
	//페이지에 해당하는 리스트 가져오기
	public List<KitDTO> getKitListPaged(int offset, int size,String sort) {
	    return shopMapper.getKitListPaged(offset, size,sort);
	}
	//키트 전체 갯수 가져오기
	public int getTotalKitCount() {
	    return shopMapper.getTotalKitCount();
	}
	//게시물 하나 들고오기
	public KitDTO getDetail(int id){
		return shopMapper.getDetail(id);
	}
	//게시물 지우기
	public void deleteShop(int id){
		shopMapper.deleteShop(id);
	};
}
