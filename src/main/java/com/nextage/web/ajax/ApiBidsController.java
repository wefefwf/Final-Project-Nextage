package com.nextage.web.ajax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
	    catch (Exception e) {
	        result.put("success", false);
	        result.put("message", "제안 등록 중 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
	    }
	}
	
	@GetMapping("/request/{requestId}")
	public List<BidDTO> getBidsByRequest(@PathVariable("requestId") Long requestId) {
		return bidService.getBidsByRequestId(requestId);
	}
	
	@PatchMapping("/{bidId}/status")
	public ResponseEntity<Map<String, Object>> updateBidStatus(@PathVariable("bidId") Long bidId, @RequestBody BidStatusUpdateDTO request) {

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
	
	// 선정 : 최종 확인 페이지 데이터 조회
	@GetMapping("/{bidId}/select-info")
	public ResponseEntity<Map<String, Object>> getSelectInfo(@PathVariable("bidId") Long bidId) {
	    Map<String, Object> result = new HashMap<>();
	    try {
	        Map<String, Object> info = bidService.getSelectInfo(bidId);
	        return ResponseEntity.ok(info);
	    } catch (IllegalArgumentException e) {
	        result.put("success", false);
	        result.put("message", e.getMessage());
	        return ResponseEntity.badRequest().body(result);
	    } catch (Exception e) {
	        result.put("success", false);
	        result.put("message", "정보 조회 중 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
	    }
	}
	
	// 결제 사전 주문 생성
	@PostMapping("/payment/create")
	public ResponseEntity<Map<String, Object>> createBidPayment(
			@RequestBody Map<String, Object> body) {
		Map<String, Object> result = new HashMap<>();
		try {
			Long bidId	   = Long.parseLong(body.get("bidId").toString());
			int totalAmount  = Integer.parseInt(body.get("totalAmount").toString());

			String orderNo = bidService.createBidOrder(bidId, totalAmount);

			result.put("success", true);
			result.put("orderNo", orderNo);
			return ResponseEntity.ok(result);

		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "주문 생성 중 오류가 발생했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}
	}

	// 결제 검증 + 선정 처리
	@PostMapping("/payment/verify")
	public ResponseEntity<Map<String, Object>> verifyBidPayment(
			@RequestBody Map<String, Object> body) {
		Map<String, Object> result = new HashMap<>();
		try {
			String orderNo	= body.get("orderNo").toString();
			String impUid	 = body.get("impUid").toString();
			int totalAmount   = Integer.parseInt(body.get("totalAmount").toString());
			Long bidId		= Long.parseLong(body.get("bidId").toString());
			Object dimensions = body.get("dimensions");

			boolean verified = bidService.verifyBidPayment(orderNo, impUid, totalAmount);
			if (!verified) {
				result.put("success", false);
				result.put("message", "결제 검증에 실패했습니다.");
				return ResponseEntity.badRequest().body(result);
			}

			// 검증 성공 시 선정 처리
			bidService.selectBidWithDimensions(bidId, dimensions);

			result.put("success", true);
			result.put("message", "선정 및 결제가 완료되었습니다.");
			return ResponseEntity.ok(result);

		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "결제 처리 중 오류가 발생했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
		}
	}
	
	// 결제 성공 후 치수 저장 + 선정 처리
	@PostMapping("/{bidId}/select")
	public ResponseEntity<Map<String, Object>> selectBid(
	        @PathVariable("bidId") Long bidId,
	        @RequestBody Map<String, Object> payload) {
	    Map<String, Object> result = new HashMap<>();
	    try {
	        // dimensions: { "raw": "총장 68cm / ..." } 형태로 넘어옴
	        Object dimensions = payload.get("dimensions");
	        bidService.selectBidWithDimensions(bidId, dimensions);
	        result.put("success", true);
	        result.put("message", "선정이 완료되었습니다.");
	        return ResponseEntity.ok(result);
	    } catch (IllegalArgumentException e) {
	        result.put("success", false);
	        result.put("message", e.getMessage());
	        return ResponseEntity.badRequest().body(result);
	    } catch (Exception e) {
	        result.put("success", false);
	        result.put("message", "선정 처리 중 오류가 발생했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
	    }
	}
	
	
	/**
     * 업체 본인의 제안 목록 조회
     */
    @GetMapping("/business/{businessId}")
    public List<BidDTO> getBidsByBusiness(@PathVariable("businessId") Long businessId) {
        return bidService.getBidsByBusinessId(businessId);
    }
 
    /**
     * 업체 본인이 자신의 제안 삭제 (소프트 딜리트: HIDDEN)
     */
    @DeleteMapping("/{bidId}")
    public ResponseEntity<Map<String, Object>> deleteBid(@PathVariable("bidId") Long bidId) {
        Map<String, Object> result = new HashMap<>();
        try {
            bidService.updateBidStatus(bidId, "HIDDEN");
            result.put("success", true);
            result.put("message", "제안이 삭제되었습니다.");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
	
	

}
