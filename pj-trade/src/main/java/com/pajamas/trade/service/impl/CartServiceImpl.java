package com.pajamas.trade.service.impl;

import com.pajamas.api.client.course.CourseClient;
import com.pajamas.api.dto.course.CourseSimpleInfoDTO;
import com.pajamas.common.exceptions.BizIllegalException;
import com.pajamas.common.utils.CollUtils;
import com.pajamas.common.utils.UserContext;
import com.pajamas.trade.domain.dto.CartAddDTO;
import com.pajamas.trade.domain.po.Cart;
import com.pajamas.trade.domain.po.CartItem;
import com.pajamas.trade.domain.vo.CartItemVO;
import com.pajamas.trade.domain.vo.CartVO;
import com.pajamas.trade.mapper.CartMapper;
import com.pajamas.trade.service.ICartItemService;
import com.pajamas.trade.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 购物车 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-01
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    private final ICartItemService cartItemService;
    private final CourseClient courseClient;

    @Override
    @Transactional
    public void addItem(CartAddDTO dto) {
        //1.获取课程信息
        List<CourseSimpleInfoDTO> courses = courseClient.getSimpleInfoList(List.of(dto.getSkuId()));
        if (CollUtils.isEmpty(courses)) {
            throw new BizIllegalException("课程不存在");
        }
        CourseSimpleInfoDTO course = courses.get(0);
        //2.获取用户id
        Long userId = UserContext.getUser();
        //3.查询购物车（用于更新）
        Long shopId = dto.getShopId();
        Cart cart = lambdaQuery().eq(Cart::getUserId, userId)
                .eq(Cart::getShopId, shopId)
                .one();
        if (cart == null) {
            //3.1为空则新增
            cart = new Cart();
            cart.setUserId(userId);
            if(shopId != null){
                cart.setShopId(shopId);
            }
            cart.setLastAccessTime(LocalDateTime.now());
            save(cart);
        }
        //4.查询购物车项
        CartItem item = cartItemService.lambdaQuery()
                .eq(CartItem::getCartId, cart.getId())
                .eq(CartItem::getSkuId, dto.getSkuId())
                .one();
        if (item == null) {
            //4.1为空则新增
            item = new CartItem();
            item.setCartId(cart.getId());
            item.setUserId(userId);
            item.setShopId(shopId);
            item.setSkuId(dto.getSkuId());
            item.setSpuId(course.getThirdCateId());
            item.setQuantity(dto.getQuantity());
            item.setActivityId(dto.getActivityId());
            item.setPriceAtAdd(course.getPrice() != null ? course.getPrice() : 0);
            cartItemService.save(item);
        } else {
            //4.2不为空则更新
            item.setActivityId(dto.getActivityId());
            item.setQuantity(item.getQuantity() + dto.getQuantity());
            item.setPriceAtAdd(course.getPrice() != null ? course.getPrice() : 0);
            cartItemService.updateById(item);
        }
        //5.更新统计字段
        recalculateCart(cart.getId());
    }

    @Override
    @Transactional
    public void removeItem(Long itemId) {
        //1.获取购物车项
        Long userId = UserContext.getUser();
        CartItem item = cartItemService.lambdaQuery()
                .eq(CartItem::getId, itemId)
                .eq(CartItem::getUserId, userId)
                .one();
        if (item == null) {
            return;
        }
        cartItemService.removeById(itemId);
        //2.更新统计字段
        recalculateCart(item.getCartId());
    }

    @Override
    @Transactional
    public void removeItems(List<Long> itemIds) {
        Long userId = UserContext.getUser();
        if (CollUtils.isEmpty(itemIds)) {
            return;
        }
        List<CartItem> items = cartItemService.lambdaQuery()
                .in(CartItem::getId, itemIds)
                .eq(CartItem::getUserId, userId)
                .list();
        if (CollUtils.isEmpty(items)) {
            return;
        }
        cartItemService.removeByIds(items.stream().map(CartItem::getId).collect(Collectors.toList()));
        recalculateCart(items.get(0).getCartId());
    }

    @Override
    public List<CartVO> getCart() {
        //1.获取用户id
        Long userId = UserContext.getUser();
        //2.查询用户所有购物车
        List<Cart> carts = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getStatus, 1)
                .list();
        //3.购物车不存在则返回空
        List<CartVO> result = new ArrayList<>();
        if (CollUtils.isEmpty(carts)) {
            return result;
        }
        //4.遍历每个购物车组装VO
        for (Cart cart : carts) {
            result.add(buildCartVO(cart));
        }
        return result;
    }

    @Override
    public CartVO getCartByShopId(Long shopId) {
        Long userId = UserContext.getUser();
        Cart cart = lambdaQuery()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getShopId, shopId)
                .eq(Cart::getStatus, 1)
                .one();
        if (cart == null) {
            return null;
        }
        return buildCartVO(cart);
    }

    private CartVO buildCartVO(Cart cart) {
        //1.更新最后访问时间
        cart.setLastAccessTime(LocalDateTime.now());
        updateById(cart);
        //2.查询购物车项
        List<CartItem> items = cartItemService.lambdaQuery()
                .eq(CartItem::getCartId, cart.getId())
                .list();
        //3.批量查询课程实时信息
        Map<Long, CourseSimpleInfoDTO> courseMap = new HashMap<>();
        List<Long> courseIds = items.stream()
                .map(CartItem::getSkuId)
                .collect(Collectors.toList());
        if (CollUtils.isNotEmpty(courseIds)) {
            List<CourseSimpleInfoDTO> courses = courseClient.getSimpleInfoList(courseIds);
            if (CollUtils.isNotEmpty(courses)) {
                courseMap = courses.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));
            }
        }
        //4.组装CartVO
        CartVO vo = new CartVO();
        vo.setId(cart.getId());
        vo.setTotalQuantity(cart.getTotalQuantity());
        vo.setItemCount(cart.getItemCount());
        List<CartItemVO> itemVOs = new ArrayList<>(items.size());
        for (CartItem item : items) {
            CartItemVO itemVO = new CartItemVO();
            BeanUtils.copyProperties(item, itemVO);
            CourseSimpleInfoDTO course = courseMap.get(item.getSkuId());
            boolean expired = true;
            if (course != null) {
                itemVO.setCourseName(course.getName());
                itemVO.setCoverUrl(course.getCoverUrl());
                itemVO.setRealPrice(course.getPrice());
                expired = course.getStatus() != 2 || (course.getPurchaseEndTime() != null
                        && course.getPurchaseEndTime().isBefore(LocalDateTime.now()));
                itemVO.setExpired(expired);
            }
            itemVOs.add(itemVO);
        }
        vo.setItems(itemVOs);
        return vo;
    }

    /**
     * 更新统计字段
     * @param cartId
     */
    private void recalculateCart(Long cartId) {
        List<CartItem> items = cartItemService.lambdaQuery()
                .eq(CartItem::getCartId, cartId)
                .eq(CartItem::getStatus, 1)
                .list();
        Cart cart = getById(cartId);
        if (cart != null) {
            cart.setItemCount(items.size());
            cart.setTotalQuantity(items.stream().mapToInt(CartItem::getQuantity).sum());
            cart.setLastAccessTime(LocalDateTime.now());
            updateById(cart);
        }
    }
}
