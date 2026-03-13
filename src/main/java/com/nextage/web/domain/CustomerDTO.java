package com.nextage.web.domain;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
	private Long customerId;
    private String loginId;
    private String email;
    private String nickname;
    private String passwordHash;
    private String phoneNumber;
    private String address;
    private String role;  
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deleted_at;
}
