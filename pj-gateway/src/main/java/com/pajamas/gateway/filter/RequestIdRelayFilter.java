package com.pajamas.gateway.filter;

import cn.hutool.core.lang.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.pajamas.common.constants.Constant.*;

@Slf4j
@Component
public class RequestIdRelayFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.生成 RequestId
        String requestId = UUID.randomUUID().toString(true);
        log.info("requestId输出了:{}",requestId);
        log.info("转发请求头: {}", exchange.getRequest().getHeaders());
        // 2.保存到日志变量池
        MDC.put(REQUEST_ID_HEADER, requestId);
        // 3.更新请求头，添加标示
        log.info("mutate 前: {}", exchange.getRequest().getHeaders());
        String path = exchange.getRequest().getPath().toString();
        exchange = exchange.mutate().request(b -> {
                    // 3.1.添加请求id标示
                    b.header(REQUEST_ID_HEADER, requestId);
                    // 3.2.添加网关标示
                    if (!path.startsWith("/ps/notify")) {
                        b.header(REQUEST_FROM_HEADER, GATEWAY_ORIGIN_NAME);
                    }
                }
        ).build();
        log.info("mutate 后: {}", exchange.getRequest().getHeaders());
        return chain.filter(exchange);
    }
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
