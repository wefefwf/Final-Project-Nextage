package com.nextage.web.domain;

import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDTO {
	private Long noticeId;
    private String title;
    private String content;
    private String target;  
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
