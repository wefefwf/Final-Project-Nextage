package com.nextage.web.ajax;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextage.web.domain.CustomerAddressUpdateDTO;
import com.nextage.web.service.CustomerService;
import com.nextage.web.userDetails.CustomerUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer")
public class ApiCustomerController {

	private final CustomerService customerService;

	@PreAuthorize("hasRole('CUSER')")
	@PatchMapping("/me/address")
	public ResponseEntity<Map<String, Object>> updateMyAddress(
			@RequestBody CustomerAddressUpdateDTO request,
			@AuthenticationPrincipal CustomerUserDetails userDetails) {

		Map<String, Object> result = new HashMap<>();

		try {
			if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
				result.put("success", false);
				result.put("message", "기본 주소를 입력해주세요.");
				return ResponseEntity.badRequest().body(result);
			}

			customerService.updateCustomerAddress(
					userDetails.getUsername(),
					request.getPostcode(),
					request.getAddress(),
					request.getAddressDetail()
			);

			result.put("success", true);
			result.put("message", "배송지가 저장되었습니다.");
			result.put("address", 
				(request.getPostcode() == null ? "" : request.getPostcode().trim()) + "#" +
				request.getAddress().trim() + "#" +
				(request.getAddressDetail() == null ? "" : request.getAddressDetail().trim())
			);

			return ResponseEntity.ok(result);

		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "배송지 저장 중 오류가 발생했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}
	}
}