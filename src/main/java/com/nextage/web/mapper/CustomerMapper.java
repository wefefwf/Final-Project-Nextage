package com.nextage.web.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.nextage.web.domain.CustomerDTO;

@Mapper
public interface CustomerMapper {
  CustomerDTO findByLoginId(String loginId);
}
