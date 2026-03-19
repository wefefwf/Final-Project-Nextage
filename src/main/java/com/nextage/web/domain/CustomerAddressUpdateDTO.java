package com.nextage.web.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerAddressUpdateDTO {
	private String postcode;
	private String address;
	private String addressDetail;
}