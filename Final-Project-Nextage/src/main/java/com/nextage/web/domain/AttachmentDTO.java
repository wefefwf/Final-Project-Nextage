package com.nextage.web.domain;

import lombok.Data;

@Data
public class AttachmentDTO {
    private Long attachId;
    private String refType;   // REQ (의뢰용)
    private Long refId;       // requestId와 매핑
    private String imageUrl;  // D드라이브 저장 경로
    private String originName;
    private boolean isThumbnail;
}