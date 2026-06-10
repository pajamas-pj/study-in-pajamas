package com.pajamas.trade.controller;

import com.pajamas.trade.domain.dto.CartAddDTO;
import com.pajamas.trade.domain.vo.CartVO;
import com.pajamas.trade.service.ICartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "购物车相关接口")
@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final ICartService cartService;

    @PostMapping("/items")
    @ApiOperation("添加商品到购物车")
    public void addItem(@Valid @RequestBody CartAddDTO dto) {
        cartService.addItem(dto);
    }

    @DeleteMapping("/items/{id}")
    @ApiOperation("删除购物车中某个商品")
    public void removeItem(@PathVariable("id") Long id) {
        cartService.removeItem(id);
    }

    @DeleteMapping("/items")
    @ApiOperation("批量删除购物车商品")
    public void removeItems(@RequestParam("ids") List<Long> ids) {
        cartService.removeItems(ids);
    }

    @GetMapping
    @ApiOperation("查询购物车内容")
    public List<CartVO> getCart() {
        return cartService.getCart();
    }

    @GetMapping("/shop/{shopId}")
    @ApiOperation("按店铺查询购物车")
    public CartVO getCartByShopId(@PathVariable("shopId") Long shopId) {
        return cartService.getCartByShopId(shopId);
    }
}
