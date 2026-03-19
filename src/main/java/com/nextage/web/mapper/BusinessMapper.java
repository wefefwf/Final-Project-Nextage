package com.nextage.web.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.nextage.web.domain.BusinessDTO;

@Mapper
public interface BusinessMapper {
	int insertBusiness(BusinessDTO business);

    BusinessDTO findByLoginId(String loginId);
    int countByLoginId(String loginId);
    int countByPhoneNumber(String phoneNumber);
    int countByCompanyName(String companyName);
    
    int updateBusiness(BusinessDTO business);
    void updateRoleToNull(String loginId);
  
}
