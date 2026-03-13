package com.nextage.web.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter 
@Setter 
@Builder 
@ToString
@NoArgsConstructor 
@AllArgsConstructor
public class BusinessDTO {
	private Long businessId;
    private String loginId;
    private String passwordHash;
    private String companyName;
    private String phoneNumber;
    private String role;   
    private String status; 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deleted_at;

}