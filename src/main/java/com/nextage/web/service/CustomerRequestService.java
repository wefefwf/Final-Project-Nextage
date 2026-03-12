package com.nextage.web.service;

import com.nextage.web.domain.AttachmentDTO;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.mapper.CustomerRequestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerRequestService {

    @Autowired
    private CustomerRequestMapper requestMapper;

    @Value("${file.upload-dir}")
    private String uploadDir; 

    // 1 & 2 & 3. 의뢰 등록, 파일 업로드, 태그 저장
    @Transactional // 여러 테이블에 저장하므로 트랜잭션을 걸어주는 것이 좋습니다.
    public void registerRequest(RequestDTO dto, MultipartFile[] files) {
        // 1. 의뢰글 먼저 저장 (requestId가 생성됨)
        requestMapper.insertRequest(dto);
        
        // [추가] 3. 태그 저장 (지수님이 입력한 tags 문자열을 쪼개서 개별 저장)
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            // 쉼표(,)를 기준으로 분리
            String[] tagArray = dto.getTags().split(",");
            for (String tag : tagArray) {
                String trimmedTag = tag.trim(); // 앞뒤 공백 제거
                if (!trimmedTag.isEmpty()) {
                    // Mapper 인터페이스에 추가한 insertTag 호출
                    requestMapper.insertTag(dto.getRequestId(), trimmedTag);
                }
            }
        }
        
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
                        attach.setImageUrl("/images/" + savedName);
                        attach.setOriginName(originalName);
                        attach.setThumbnail(i == 0); 
                        
                        requestMapper.insertAttachment(attach);
                    } catch (Exception e) { 
                        e.printStackTrace(); 
                    }
                }
            }
        }
    }

    // 1. 전체 의뢰 목록 조회
    public List<RequestDTO> getAllRequests() {
        return requestMapper.selectAllRequests();
    }

    // 2. 의뢰 상세 조회
    public RequestDTO getRequestDetail(Long requestId) {
        return requestMapper.selectRequestDetail(requestId);
    }

    // 4. 내 의뢰 목록 조회
    public List<RequestDTO> getRequestsByCustomerId(Long customerId) {
        return requestMapper.selectRequestsByCustomerId(customerId);
    }

    // 5. 의뢰 상태 변경
    public void updateRequestStatus(Long requestId, String status) {
        requestMapper.updateStatus(requestId, status);
    }

 // 6. 의뢰글 삭제
    @Transactional // 데이터 삭제 시에는 반드시 트랜잭션을 걸어주세요!
    public void removeRequest(Long requestId) {
        // 1. 태그 삭제 (이 부분이 추가되어야 합니다)
        requestMapper.deleteTagsByRequestId(requestId);
        
        // 2. 첨부파일 DB 기록 삭제
        requestMapper.deleteAttachmentsByRequestId(requestId);
        
        // 3. 실제 의뢰글 삭제
        requestMapper.deleteRequest(requestId);
    }
    
    // [메인 페이지 트렌드 박스용 데이터 연동]
    public List<RequestDTO> getUrgentRequests() {
        return requestMapper.selectUrgentRequests();
    }

    public List<RequestDTO> getNewRequests() {
        return requestMapper.selectNewRequests();
    }

    public List<RequestDTO> getBestReviews() {
        return requestMapper.selectBestReviews();
    }

    public List<RequestDTO> getRequestsByCategory(String category) {
        return requestMapper.selectRequestsByCategory(category);
    }
}