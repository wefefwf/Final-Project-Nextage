package com.nextage.web.mapper;

import com.nextage.web.domain.AttachmentDTO;
import com.nextage.web.domain.RequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CustomerRequestMapper {
    // 1. 의뢰 등록 (ID 자동 생성을 위해 사용)
    void insertRequest(RequestDTO requestDto);
    
    // 2. 사진 등록
    void insertAttachment(AttachmentDTO attachmentDto);
    
    // 3. 전체 목록 조회 (Service에서 호출하는 이름에 맞춤)
    List<RequestDTO> selectAllRequests();
    
    // 4. 내 의뢰 목록 조회
    List<RequestDTO> selectRequestsByCustomerId(Long customerId);
    
    // 5. 상태 변경 (파라미터가 2개 이상일 땐 @Param을 붙여야 MyBatis가 인식해요)
    void updateStatus(@Param("requestId") Long requestId, @Param("status") String status);
    
    // 6. 삭제 기능
    void deleteRequest(Long requestId);
    void deleteAttachmentsByRequestId(Long requestId);
}