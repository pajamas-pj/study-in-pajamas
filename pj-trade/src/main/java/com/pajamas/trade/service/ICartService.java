package com.pajamas.trade.service;

import com.pajamas.trade.domain.dto.CartAddDTO;
import com.pajamas.trade.domain.po.Cart;
import com.pajamas.trade.domain.vo.CartVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 购物车 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-01
 */
public interface ICartService extends IService<Cart> {

    void addItem(CartAddDTO dto);

    void removeItem(Long itemId);

    void removeItems(List<Long> itemIds);

    List<CartVO> getCart();

    CartVO getCartByShopId(Long shopId);

    void updateChecked(Long id, Integer checked);

    void batchUpdateChecked(List<Long> ids, Integer checked);

    void updateQuantity(Long id, Integer quantity);
}
