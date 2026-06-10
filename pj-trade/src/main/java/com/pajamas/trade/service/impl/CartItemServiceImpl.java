package com.pajamas.trade.service.impl;

import com.pajamas.trade.domain.po.CartItem;
import com.pajamas.trade.mapper.CartItemMapper;
import com.pajamas.trade.service.ICartItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 购物车项 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-01
 */
@Service
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements ICartItemService {

}
