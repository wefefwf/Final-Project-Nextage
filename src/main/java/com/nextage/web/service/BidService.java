package com.nextage.web.service;

import java.util.List;

import org.springframework.stereotype.Service;

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

}
