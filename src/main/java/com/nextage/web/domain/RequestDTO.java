package com.nextage.web.domain;

import java.util.List;
import lombok.Data;

@Data
public class RequestDTO {
    private Long requestId;
    private Long customerId;
    private Long categoryId;
    private String title;
    private String description;
    private Long hopePrice;
    private String requestedDueDate; 
    private String status;           
    private String createdAt;
    private String updatedAt;

    // Join을 통해 가져올 추가 정보 (화면 표시용)
    private String categoryName;     // 카테고리 ID 대신 "2D Reform" 출력용
    private String customerNickname; // 작성자 이름 출력용
    
    // 사진 정보를 담기 위한 리스트
    private List<AttachmentDTO> attachmentList;
    
    // 메인 화면이나 목록에서 보여줄 대표 이미지 (첫 번째 사진)
    private String thumbnailPath; 
}