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
    
    // [해결] 3-2. 의뢰 상세 조회 (상세 페이지 연동을 위해 반드시 필요!)
    // image_a4d126.png의 에러를 잡는 핵심 코드입니다.
    RequestDTO selectRequestDetail(Long requestId);
    
    // 4. 내 의뢰 목록 조회
    List<RequestDTO> selectRequestsByCustomerId(Long customerId);
    
    // 5. 상태 변경 (파라미터가 2개 이상일 땐 @Param을 붙여야 MyBatis가 인식해요)
    void updateStatus(@Param("requestId") Long requestId, @Param("status") String status);
    
    // 6. 삭제 기능
    void deleteAttachmentsByRequestId(Long requestId); // 첨부파일 먼저 삭제
    void deleteRequest(Long requestId); // 의뢰글 삭제
    
 // ===========================================================
    // [메인 페이지 트렌드 박스용 추가 메서드]
    // XML의 id값과 반드시 일치해야 데이터가 뜹니다!
    // ===========================================================
    
 // 1. 마감 임박 의뢰 목록 조회 (URGENT)
    List<RequestDTO> selectUrgentRequests();

    // 2. 최신 의뢰 목록 조회 (NEW ARRIVALS)
    List<RequestDTO> selectNewRequests();

    // 3. 베스트 후기 목록 조회 (BEST REVIEWS)
    List<RequestDTO> selectBestReviews();
}