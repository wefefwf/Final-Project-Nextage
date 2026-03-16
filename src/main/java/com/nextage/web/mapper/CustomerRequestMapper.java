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
    
    // 3. 전체 목록 조회
    List<RequestDTO> selectAllRequests();
    
    // 3-2. 의뢰 상세 조회
    RequestDTO selectRequestDetail(Long requestId);
    
    // 4. 내 의뢰 목록 조회
    List<RequestDTO> selectRequestsByCustomerId(Long customerId);
    
    // 5. 상태 변경
    void updateStatus(@Param("requestId") Long requestId, @Param("status") String status);
    
    // 6. 삭제 기능
    // [추가] 자식 테이블인 태그를 먼저 삭제하기 위해 필요한 메서드입니다.
    void deleteTagsByRequestId(Long requestId); 
    
    //업데이트
    void updateRequest(RequestDTO dto);
    
    void deleteAttachmentsByRequestId(Long requestId); // 첨부파일 먼저 삭제
    void deleteRequest(Long requestId); // 의뢰글 삭제
    
    // 의뢰 등록 시 태그를 하나씩 저장하는 메서드
    void insertTag(@Param("requestId") Long requestId, @Param("tagName") String tagName);
    
    // 유저: 업체 선정 결제 후 치수 정보 저장
    void updateDimensions(@Param("requestId") Long requestId, @Param("dimensions") String dimensions);
    
    // ===========================================================
    // [메인 페이지 트렌드 박스용 추가 메서드]
    // ===========================================================
    
    // 1. 마감 임박 의뢰 목록 조회 (URGENT)
    List<RequestDTO> selectUrgentRequests();

    // 2. 최신 의뢰 목록 조회 (NEW ARRIVALS)
    List<RequestDTO> selectNewRequests();

    // 3. 베스트 후기 목록 조회 (BEST REVIEWS)
    List<RequestDTO> selectBestReviews();

    // 카테고리별 의뢰 조회
    List<RequestDTO> selectRequestsByCategory(String category);
}