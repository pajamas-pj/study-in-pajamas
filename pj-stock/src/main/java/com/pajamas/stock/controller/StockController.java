package com.pajamas.stock.controller;

import com.pajamas.stock.domain.dto.StockFormDTO;
import com.pajamas.stock.domain.vo.StockVO;
import com.pajamas.stock.service.IStockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "库存管理")
@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockController {

    private final IStockService stockService;

    @ApiOperation("新增库存")
    @PostMapping
    public void createStock(@RequestBody StockFormDTO dto) {
        stockService.createStock(dto);
    }

    @ApiOperation("修改库存")
    @PutMapping
    public void updateStock(@RequestBody StockFormDTO dto) {
        stockService.updateStock(dto);
    }

    @ApiOperation("查询库存")
    @GetMapping("/{skuId}")
    public StockVO getStock(@PathVariable Long skuId) {
        return stockService.getStock(skuId);
    }

    @ApiOperation("批量查询库存")
    @GetMapping
    public List<StockVO> listStocks(@RequestParam("ids") List<Long> skuIds) {
        return stockService.listStocks(skuIds);
    }

    @ApiOperation("删除库存")
    @DeleteMapping("/{skuId}")
    public void deleteStock(@PathVariable Long skuId) {
        stockService.deleteStock(skuId);
    }
}
