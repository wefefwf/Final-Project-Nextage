package com.nextage.web.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.nextage.web.domain.BusinessDTO;

@Mapper
public interface BusinessMapper {
  BusinessDTO findByLoginId(String loginId);
  
}
