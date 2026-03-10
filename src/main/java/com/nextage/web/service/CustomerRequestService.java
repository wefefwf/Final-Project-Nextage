package com.nextage.web.service;

import com.nextage.web.domain.AttachmentDTO;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.mapper.CustomerRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerRequestService {

    @Autowired
    private CustomerRequestMapper requestMapper;

    @Value("${file.upload-dir}")
    private String uploadDir; // application.properties의 경로 호출

    // 1 & 2. 의뢰 등록 및 파일 업로드
    public void registerRequest(RequestDTO dto, MultipartFile[] files) {
        // 1. 의뢰글 먼저 저장
        requestMapper.insertRequest(dto);
        
        // 2. 사진들 반복문 돌리며 D드라이브 저장 및 DB 기록
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i] != null && !files[i].isEmpty()) {
                    String originalName = files[i].getOriginalFilename();
                    String savedName = UUID.randomUUID().toString() + "_" + originalName;
                    
                    try {
                        // 실제 파일 저장
                        files[i].transferTo(new File(uploadDir + "/" + savedName));
                        
                        // DB 저장용 객체 생성
                        AttachmentDTO attach = new AttachmentDTO();
                        attach.setRefType("REQ");
                        attach.setRefId(dto.getRequestId());
                        attach.setImageUrl("/upload/" + savedName);
                        attach.setOriginName(originalName);
                        attach.setThumbnail(i == 0); // 첫 번째 사진을 대표 이미지로
                        
                        requestMapper.insertAttachment(attach);
                    } catch (Exception e) { 
                        e.printStackTrace(); 
                    }
                }
            }
        }
    }

    // [추가] 1. 전체 의뢰 목록 조회 (게시판 메인)
    public List<RequestDTO> getAllRequests() {
        return requestMapper.selectAllRequests();
    }

    // [추가] 2. 의뢰 상세 조회 (상세 페이지용 - 새로 추가된 부분!)
    public RequestDTO getRequestDetail(Long requestId) {
        return requestMapper.selectRequestDetail(requestId);
    }

    // [추가] 4. 내 의뢰 목록 조회 (사용자 본인 글만)
    public List<RequestDTO> getRequestsByCustomerId(Long customerId) {
        return requestMapper.selectRequestsByCustomerId(customerId);
    }

    // [추가] 5. 의뢰 상태 변경 (OPEN -> SELECTED 등)
    public void updateRequestStatus(Long requestId, String status) {
        requestMapper.updateStatus(requestId, status);
    }

    // [추가] 6. 의뢰글 삭제 (이미지 제외 DB 레코드 삭제 우선)
    public void removeRequest(Long requestId) {
        // 먼저 첨부파일 레코드부터 삭제 (외래키 제약조건 고려)
        requestMapper.deleteAttachmentsByRequestId(requestId);
        // 의뢰글 삭제
        requestMapper.deleteRequest(requestId);
    }
    
 // ===========================================================
    // [메인 페이지 트렌드 박스용 데이터 연동]
    // ===========================================================

    // 1. 마감 임박 의뢰 조회 (URGENT)
    public List<RequestDTO> getUrgentRequests() {
        return requestMapper.selectUrgentRequests();
    }

    // 2. 최신 의뢰 조회 (NEW ARRIVALS)
    public List<RequestDTO> getNewRequests() {
        return requestMapper.selectNewRequests();
    }

    // 3. 베스트 후기 조회 (BEST REVIEWS)
    public List<RequestDTO> getBestReviews() {
        return requestMapper.selectBestReviews();
    }
 // [추가] 카테고리별 의뢰 조회
    public List<RequestDTO> getRequestsByCategory(String category) {
        return requestMapper.selectRequestsByCategory(category);
    }
}