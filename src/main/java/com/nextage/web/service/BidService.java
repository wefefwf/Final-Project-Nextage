package com.nextage.web.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextage.web.domain.BidDTO;
import com.nextage.web.mapper.BidsMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BidService {

	private final BidsMapper bidsMapper;

	public int addBid(BidDTO bid) {
			return bidsMapper.insertBid(bid);
	}
	
	public List<BidDTO> getBidsByRequestId(Long requestId) {
		return bidsMapper.selectBidsByRequestId(requestId);
	}
	
	public List<BidDTO> getBidsByBusinessId(Long businessId) {
		return bidsMapper.selectBidsByBusinessId(businessId);
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

}
