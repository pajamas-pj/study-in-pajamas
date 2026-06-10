package com.pajamas.trade.controller;

import com.pajamas.trade.domain.dto.OrderCreateDTO;
import com.pajamas.trade.domain.vo.OrderCreateVO;
import com.pajamas.trade.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "订单相关接口")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @GetMapping("/idempotent")
    @ApiOperation("获取幂等token")
    public String getIdempotentToken() {
        return orderService.getIdempotentToken();
    }

    @PostMapping
    @ApiOperation("创建订单")
    public OrderCreateVO createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return orderService.createOrder(dto);
    }


}
