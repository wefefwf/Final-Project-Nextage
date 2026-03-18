package com.nextage.web.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BidDTO {

	private Long bidId;
	private Long requestId;
	private Long businessId;
	private Long price;
	private LocalDate expectedDueDate;
	private Boolean asAvailable;
	private String description;
	private String status;
	private LocalDateTime selectedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	private String businessLoginId;
	private String companyName;

}
