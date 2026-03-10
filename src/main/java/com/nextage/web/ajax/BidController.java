package com.nextage.web.ajax;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextage.web.domain.BidDTO;
import com.nextage.web.service.BidService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/bids")
public class BidController {

	private final BidService bidService;

	@PostMapping("/")
	public ResponseEntity<String> createBid(@RequestBody BidDTO bid) {
		int result = bidService.addBid(bid);
		if (result == 1) {
			return ResponseEntity.ok("Bid created successfully");
		} else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create bid");
		}
	}

	@GetMapping("/request/{requestId}")
	public List<BidDTO> getBidsByRequest(@PathVariable("requestId") Long requestId) {
		return bidService.getBidsByRequestId(requestId);
	}

}
