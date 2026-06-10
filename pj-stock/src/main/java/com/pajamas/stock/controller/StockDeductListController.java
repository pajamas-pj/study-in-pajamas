package com.pajamas.stock.controller;

import com.pajamas.api.dto.stock.StockDeductListFormDTO;
import com.pajamas.stock.service.IStockDeductListService;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * TCC事务记录List 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-07
 */
@Api(tags = "TCC批量库存事务管理")
@RestController
@RequestMapping("/stock-TCC/list")
@RequiredArgsConstructor
public class StockDeductListController {

    private final IStockDeductListService stockDeductListService;

    @ApiOperation("批量 Try — 预扣库存")
    @PostMapping("/try")
    public boolean batchTry(@RequestBody StockDeductListFormDTO dto) {
        return stockDeductListService.batchTry(null, dto.getItems(), dto.getOrderNo(), dto.getExpireTime());
    }

    @ApiOperation("批量 Confirm — 确认扣减")
    @PostMapping("/confirm")
    public boolean batchConfirm(@RequestBody BusinessActionContext context) {
        return stockDeductListService.batchConfirm(context);
    }

    @ApiOperation("批量 Cancel — 回滚预扣")
    @PostMapping("/cancel")
    public boolean batchCancel(@RequestBody BusinessActionContext context) {
        return stockDeductListService.batchCancel(context);
    }
}
