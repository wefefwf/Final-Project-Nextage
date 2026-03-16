package com.nextage.web.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

	int countActiveBidByRequestIdAndBusinessId(@Param("requestId") Long requestId,
			@Param("businessId") Long businessId);

	void insertBidOrder(@Param("orderNo") String orderNo, @Param("customerId") Long customerId,
			@Param("businessId") Long businessId, @Param("bidId") Long bidId, @Param("totalAmount") int totalAmount, @Param("dueDate") LocalDate dueDate);

	Integer selectTotalAmountByOrderNo(@Param("orderNo") String orderNo);

	void updateBidOrderStatus(@Param("orderNo") String orderNo, @Param("impUid") String impUid,
			@Param("paymentStatus") String paymentStatus);
	
	// 결제 시 주문내역에서 order_items 필요
	void insertBidOrderItem(@Param("orderId") Long orderId, @Param("totalAmount") int totalAmount);
	Long selectOrderIdByOrderNo(@Param("orderNo") String orderNo);
	
	Map<String, Object> selectCustomerInfoByRequestId(@Param("requestId") Long requestId);

}
