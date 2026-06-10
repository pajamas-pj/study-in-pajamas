package com.pajamas.api.client.stock.fallback;

import com.pajamas.api.client.stock.StockDeductClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class StockDeductFallback implements FallbackFactory<StockDeductClient> {
    @Override
    public StockDeductClient create(Throwable cause) {
        return null;
    }
}
