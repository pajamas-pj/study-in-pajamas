package com.pajamas.trade.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pajamas.common.utils.BeanUtils;
import com.pajamas.common.utils.UserContext;
import com.pajamas.trade.domain.po.OrderLog;
import com.pajamas.trade.mapper.OrderLogMapper;
import com.pajamas.trade.service.IOrderLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单状态日志 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-02
 */
@Service
public class OrderLogServiceImpl extends ServiceImpl<OrderLogMapper, OrderLog> implements IOrderLogService {
    @Override
    public void createOrderLog(OrderLog orderLog) {
        OrderLog log = BeanUtils.copyBean(orderLog, OrderLog.class);
        log.setOperator(String.valueOf(UserContext.getUser()));
        save(log);
    }
}
