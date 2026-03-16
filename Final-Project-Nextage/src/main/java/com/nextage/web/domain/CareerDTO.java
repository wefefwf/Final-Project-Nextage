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
public class CareerDTO {
	
	private Long careerId;        // career_id
    private Long bizinfoId;       // bizinfo_id (FK)
    private String workDescription; // 경력 상세 내용
    
}