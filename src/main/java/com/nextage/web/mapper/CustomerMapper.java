package com.nextage.web.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.nextage.web.domain.CustomerDTO;

@Mapper
public interface CustomerMapper {
  int insertCustomer(CustomerDTO customer); 

  CustomerDTO findByLoginId(String loginId);
  int countByLoginId(String loginId);
  int countByNickname(String nickname);
  int countByPhone(String phoneNumber);
  int countByEmail(String email);
  int updateCustomer(CustomerDTO customer);
  void updateRoleToNull(String loginId);
  
  // 주소(배송지)변경
  int updateCustomerAddress(CustomerDTO customer);
  
}
