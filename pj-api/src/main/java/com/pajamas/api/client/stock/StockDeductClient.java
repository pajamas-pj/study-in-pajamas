package com.pajamas.api.client.stock;

import com.pajamas.api.client.stock.fallback.StockDeductFallback;
import com.pajamas.api.dto.stock.StockTryDeductDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@FeignClient(name = "stock-service", contextId = "stock-deduct", path = "/stock-TCC", fallbackFactory = StockDeductFallback.class)
public interface StockDeductClient {

    @ApiOperation("TCC Try — 预扣库存")
    @PostMapping("/try")
    boolean tryDeduct(@RequestBody StockTryDeductDTO dto);
}
