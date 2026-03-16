package com.nextage.web.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nextage.web.domain.BidDTO;

@Mapper
public interface BidsMapper {

	int insertBid(BidDTO bid);

	List<BidDTO> selectBidsByRequestId(Long requestId);

	List<BidDTO> selectBidsByBusinessId(Long businessId);

	BidDTO selectBidById(Long bidId);

	void updateBidStatus(@Param("bidId") Long bidId, @Param("status") String status);

	void resetOtherBidsToRejected(@Param("requestId") Long requestId, @Param("selectedBidId") Long selectedBidId);

	Integer selectLowestBidPriceByRequestId(Long requestId);

	int countActiveBidByRequestIdAndBusinessId(@Param("requestId") Long requestId, @Param("businessId") Long businessId);

}
