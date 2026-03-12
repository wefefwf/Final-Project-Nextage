package com.nextage.web.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BizinfoDTO {

	private Long bizinfoId;      // bizinfo_id
    private Long businessId;     // business_id (FK)
    private String title;        // 한 줄 소개 제목
    private String bizDescription; // 업체 상세 설명
    private String location;     // 활동 지역
    private String profileImage; // 프로필 이미지 경로
    
}