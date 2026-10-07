package com.pajamas.trade.controller;


import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "订单日志相关接口")
@RestController
@RequestMapping("/orders/log")
@RequiredArgsConstructor
public class OrderLogController {

}
