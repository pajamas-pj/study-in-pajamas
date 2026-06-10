package com.pajamas.api.client.stock;

import com.pajamas.api.client.stock.fallback.StockDeductListFallback;
import com.pajamas.api.dto.stock.StockDeductListFormDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "stock-service", contextId = "stock-deduct-list", path = "/stock-TCC/list", fallbackFactory = StockDeductListFallback.class)
public interface StockDeductListClient {

    @ApiOperation("TCC 批量预扣库存")
    @PostMapping("/try")
    boolean batchTry(@RequestBody StockDeductListFormDTO dto);
}
