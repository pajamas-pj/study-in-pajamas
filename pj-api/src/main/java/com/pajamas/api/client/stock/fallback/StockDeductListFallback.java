package com.pajamas.api.client.stock.fallback;

import com.pajamas.api.client.stock.StockDeductListClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class StockDeductListFallback implements FallbackFactory<StockDeductListClient> {
    @Override
    public StockDeductListClient create(Throwable cause) {
        return null;
    }
}
