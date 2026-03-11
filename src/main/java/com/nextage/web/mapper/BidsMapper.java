package com.nextage.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nextage.web.domain.BidDTO;

@Mapper
public interface BidsMapper {

	List<BidDTO> selectBidsByRequestId(@Param("requestId") Long requestId);
	
	int insertBid(BidDTO bid);
	
	List<BidDTO> selectBidsByBusinessId(@Param("businessId") Long businessId);
	
	int updateBidStatus(@Param("bidId") Long bidId, @Param("status") String status);

    int resetOtherBidsToRejected(@Param("requestId") Long requestId, @Param("selectedBidId") Long selectedBidId);

    BidDTO selectBidById(@Param("bidId") Long bidId);

}
