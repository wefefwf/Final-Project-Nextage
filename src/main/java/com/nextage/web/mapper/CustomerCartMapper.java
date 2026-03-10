package com.nextage.web.mapper;

import com.nextage.web.domain.CartDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerCartMapper {

    Long selectCartIdByCustomerId(@Param("customerId") Long customerId);

    void insertCart(@Param("customerId") Long customerId);

    Long selectLastInsertCartId(@Param("customerId") Long customerId);

    List<CartDTO> selectCartItems(@Param("cartId") Long cartId);

    Long selectDuplicateCartItemId(@Param("cartId") Long cartId,
                                   @Param("kitId") Long kitId);

    void insertCartItem(@Param("cartId") Long cartId,
                        @Param("productId") Long productId,
                        @Param("quantity") int quantity);

    void updateCartItemQuantityAdd(@Param("cartItemId") Long cartItemId,
                                   @Param("quantity") int quantity);

    void updateCartItemQuantity(@Param("cartItemId") Long cartItemId,
                                @Param("quantity") int quantity);

    void deleteCartItem(@Param("cartItemId") Long cartItemId);

    void deleteCartItemsByIds(@Param("ids") List<Long> ids,
                               @Param("cartId") Long cartId);
}