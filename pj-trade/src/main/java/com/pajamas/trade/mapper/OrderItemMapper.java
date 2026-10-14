package com.pajamas.trade.mapper;

import com.pajamas.trade.domain.po.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 订单明细 Mapper 接口
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-02
 */
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("SELECT course_id FROM order_item WHERE order_id = #{orderId}")
    List<Long> queryCourseIdsByOrderId(Long orderId);
}
