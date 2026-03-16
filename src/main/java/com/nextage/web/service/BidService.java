package com.nextage.web.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextage.web.domain.BidDTO;
import com.nextage.web.domain.RequestDTO;
import com.nextage.web.mapper.BidsMapper;
import com.nextage.web.mapper.CustomerRequestMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BidService {

	private final BidsMapper bidsMapper;
	private final CustomerRequestMapper requestMapper;

	public List<BidDTO> getBidsByRequestId(Long requestId) {
		return bidsMapper.selectBidsByRequestId(requestId);
	}
	
	public List<BidDTO> getBidsByBusinessId(Long businessId) {
		return bidsMapper.selectBidsByBusinessId(businessId);
	}
	
	@Transactional
    public int addBid(BidDTO bid) {
        // 1. 의뢰글 조회
        RequestDTO request = requestMapper.selectRequestDetail(bid.getRequestId());
        if (request == null) {
            throw new IllegalArgumentException("존재하지 않는 의뢰글입니다.");
        }

        // 2. 의뢰 상태 검증
        if (!"OPEN".equals(request.getStatus())) {
            throw new IllegalArgumentException("현재 제안 가능한 상태의 의뢰글이 아닙니다.");
        }

        // 3. 가격 검증
        if (bid.getPrice() == null || bid.getPrice() <= 0) {
            throw new IllegalArgumentException("제안 금액이 올바르지 않습니다.");
        }

        Long desiredPrice = request.getHopePrice();
        if (desiredPrice == null || desiredPrice <= 0) {
            throw new IllegalArgumentException("희망가격 정보가 올바르지 않습니다.");
        }

        // 4. 희망가격 기준 하한선 계산
        int minAllowedPrice = (int) Math.ceil(desiredPrice * 0.95);

        // 정책
         if (bid.getPrice() < minAllowedPrice) {
             throw new IllegalArgumentException("제안 금액은 최소 " + minAllowedPrice + "원 이상이어야 합니다.");
         }

        // 5. 기존 최저가 조회
        Integer lowestBidPrice = bidsMapper.selectLowestBidPriceByRequestId(bid.getRequestId());

        if (lowestBidPrice != null && bid.getPrice() > lowestBidPrice) {
            throw new IllegalArgumentException("기존 최저가보다 낮은 금액으로만 제안할 수 있습니다. 현재 최저가: " + lowestBidPrice + "원");
        }

        // 6. 업체당 1회 제안 정책일 경우
        int exists = bidsMapper.countActiveBidByRequestIdAndBusinessId(bid.getRequestId(), bid.getBusinessId());
        if (exists > 0) {
            throw new IllegalArgumentException("이미 해당 의뢰글에 제안한 업체입니다.");
        }

        // 7. 저장
        return bidsMapper.insertBid(bid);
    }

    public BidDTO getBidById(Long bidId) {
        return bidsMapper.selectBidById(bidId);
    }

    @Transactional
    public void updateBidStatus(Long bidId, String status) {
        BidDTO bid = bidsMapper.selectBidById(bidId);

        if (bid == null) {
            throw new IllegalArgumentException("존재하지 않는 제안입니다.");
        }

        if ("SELECTED".equals(status)) {
            bidsMapper.updateBidStatus(bidId, "SELECTED");
            bidsMapper.resetOtherBidsToRejected(bid.getRequestId(), bidId);
            return;
        }

        if ("REJECTED".equals(status)) {
            bidsMapper.updateBidStatus(bidId, "REJECTED");
            return;
        }

        if ("HIDDEN".equals(status)) {
            bidsMapper.updateBidStatus(bidId, "HIDDEN");
            return;
        }

        throw new IllegalArgumentException("허용되지 않는 상태값입니다.");
    }
    
    
    // select-info: request + bid + customer 조합해서 반환
    public Map<String, Object> getSelectInfo(Long bidId) {
        // 1. bid 조회 → requestId, businessId 추출
        // 2. request 조회 (title, description, hopePrice, hopePeriod)
        // 3. customer 조회 (address, phoneNumber) - request의 customerId로
        // 4. Map으로 조합해서 반환
        
        Map<String, Object> info = new HashMap<>();
        
        BidDTO bid = bidsMapper.selectBidById(bidId);
        if (bid == null) throw new IllegalArgumentException("제안 정보를 찾을 수 없습니다.");
        
        RequestDTO requestInfo = requestMapper.selectRequestDetail(bid.getRequestId());
//        CustomerDTO customerInfo = customerMapper.findAddressAndPhoneByRequestId(bid.getRequestId());
        Map<String, Object> customerInfo = null;
        
        info.put("bid", bid);
        info.put("request", requestInfo);
        info.put("customer", customerInfo);
        
        return info;
    }

    // select: 트랜잭션으로 치수 저장 + bid 상태 SELECTED 동시 처리
    @Transactional
    public void selectBidWithDimensions(Long bidId, Object dimensions) {
    	BidDTO bid = bidsMapper.selectBidById(bidId);
    	if (bid == null) throw new IllegalArgumentException("제안 정보를 찾을 수 없습니다.");
    	
    	// 1. request.dimensions 저장
    	try {
    		String dimensionsJson = new ObjectMapper().writeValueAsString(dimensions);
    		requestMapper.updateDimensions(bid.getRequestId(), dimensionsJson);
    	} catch (Exception e) {
    		throw new RuntimeException("치수 저장 중 오류가 발생했습니다.");
    	}
    	
    	// 2. bid 상태 SELECTED
    	bidsMapper.updateBidStatus(bidId, "SELECTED");  // ← updateStatus → updateBidStatus
    	bidsMapper.resetOtherBidsToRejected(bid.getRequestId(), bidId);
    }
    
    // 주문 사전 생성
    public String createBidOrder(Long bidId, int totalAmount) {
    	BidDTO bid = bidsMapper.selectBidById(bidId);
    	
    	// bid → requestId → customerId 조회
    	RequestDTO request = requestMapper.selectRequestDetail(bid.getRequestId());
    	
    	String orderNo = "BID_"
    		+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    		+ "_" + (int)(Math.random() * 9000 + 1000);

    	// DB에 orderNo + totalAmount 저장 (검증용)
    	bidsMapper.insertBidOrder(orderNo, request.getCustomerId(), bid.getBusinessId(), bidId, totalAmount);
    	return orderNo;
    }

    // 금액 검증 (+주문내역에서 order_items 필요)
    @Transactional
    public boolean verifyBidPayment(String orderNo, String impUid, int paidAmount) {
    	Integer savedAmount = bidsMapper.selectTotalAmountByOrderNo(orderNo);
    	if (savedAmount == null) return false;
    	if (savedAmount != paidAmount) return false;

    	bidsMapper.updateBidOrderStatus(orderNo, impUid, "PAID");

    	Long orderId = bidsMapper.selectOrderIdByOrderNo(orderNo);
    	System.out.println("orderId: " + orderId);
    	
    	if (orderId != null) {
    		bidsMapper.insertBidOrderItem(orderId, paidAmount);
    		System.out.println("insertBidOrderItem 완료");
    	}

    	return true;
    }
    

}
