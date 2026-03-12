package com.nextage.web.ajax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextage.web.domain.BidDTO;
import com.nextage.web.domain.BidStatusUpdateDTO;
import com.nextage.web.service.BidService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/bids")
public class ApiBidsController {

	private final BidService bidService;

	@PostMapping("/")
	public ResponseEntity<Map<String, Object>> createBid(@RequestBody BidDTO bid) {
	    Map<String, Object> result = new HashMap<>();

	    try {
	        int saved = bidService.addBid(bid);

	        result.put("success", true);
	        result.put("message", "제안이 등록되었습니다.");
	        result.put("data", bid);

	        return ResponseEntity.ok(result);

	    } catch (IllegalArgumentException e) {
	        result.put("success", false);
	        result.put("message", e.getMessage());
	        return ResponseEntity.badRequest().body(result);

	    } 
//	    catch (Exception e) {
//	        result.put("success", false);
//	        result.put("message", "제안 등록 중 오류가 발생했습니다.");
//	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
//	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        result.put("success", false);
	        result.put("message", e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
	    }
	    
	}

	@GetMapping("/request/{requestId}")
	public List<BidDTO> getBidsByRequest(@PathVariable("requestId") Long requestId) {
		return bidService.getBidsByRequestId(requestId);
	}

	@PatchMapping("/{bidId}/status")
	public ResponseEntity<Map<String, Object>> updateBidStatus(@PathVariable("bidId") Long bidId,
			@RequestBody BidStatusUpdateDTO request) {

		Map<String, Object> result = new HashMap<>();

		try {
			bidService.updateBidStatus(bidId, request.getStatus());
			result.put("success", true);
			result.put("message", "상태가 변경되었습니다.");
			return ResponseEntity.ok(result);

		} catch (IllegalArgumentException e) {
			result.put("success", false);
			result.put("message", e.getMessage());
			return ResponseEntity.badRequest().body(result);

		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "상태 변경 중 오류가 발생했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}
	}

}
