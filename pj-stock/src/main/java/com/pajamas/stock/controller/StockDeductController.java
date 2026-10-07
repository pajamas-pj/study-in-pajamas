package com.pajamas.stock.controller;

import com.pajamas.api.dto.stock.StockTryDeductDTO;
import com.pajamas.stock.service.IStockDeductService;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "TCC库存事务管理")
@RestController
@RequestMapping("/stock-TCC")
@RequiredArgsConstructor
public class StockDeductController {

    private final IStockDeductService stockDeductService;

    @ApiOperation("TCC Try — 预扣库存")
    @PostMapping("/try")
    public boolean tryDeduct(@RequestBody StockTryDeductDTO dto) {
        return stockDeductService.tryDeduct(null, dto.getSkuId(), dto.getQuantity(), dto.getOrderNo(), dto.getExpireTime());
    }

    @ApiOperation("TCC Confirm — 确认扣减")
    @PostMapping("/confirm")
    public boolean confirm(@RequestBody BusinessActionContext context) {
        return stockDeductService.confirm(context);
    }

    @ApiOperation("TCC Cancel — 回滚预扣")
    @PostMapping("/cancel")
    public boolean cancel(@RequestBody BusinessActionContext context) {
        return stockDeductService.cancel(context);
    }

}
