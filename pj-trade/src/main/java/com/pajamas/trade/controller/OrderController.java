package com.pajamas.trade.controller;

import com.pajamas.trade.domain.dto.OrderCreateDTO;
import com.pajamas.trade.domain.vo.OrderConfirmVO;
import com.pajamas.trade.domain.vo.OrderCreateVO;
import com.pajamas.trade.domain.vo.OrderVO;
import com.pajamas.trade.service.IOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "订单相关接口")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @ApiOperation("获取幂等token")
    @GetMapping("/idempotent")
    public String getIdempotentToken() {
        return orderService.getIdempotentToken();
    }
    @ApiOperation("结算预下单接口，生成订单id，确认订单可用优惠券信息")
    @GetMapping("/prePlaceOrder")
    public OrderConfirmVO prePlaceOrder(@RequestParam("courseIds") List<Long> courseIds) {
        return orderService.prePlaceOrder(courseIds);
    }
    @ApiOperation("创建订单")
    @PostMapping("/placeOrder")
    public OrderCreateVO createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return orderService.createOrder(dto);
    }
    @ApiOperation("根据id查询订单详细信息")
    @GetMapping("/{id}")
    public OrderVO queryOrderById(@ApiParam("订单id")@PathVariable("id") Long id){
        return orderService.queryOrderById(id);
    }
}
