package com.pajamas.stock.mapper;

import com.pajamas.stock.domain.po.StockDeduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * TCC事务记录 Mapper 接口
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-03
 */
public interface StockDeductMapper extends BaseMapper<StockDeduct> {
    //查询
    @Select("SELECT sku_id , quantity FROM stock_deduct WHERE tx_id = #{txId} AND branch_id = #{branchId}")
    StockDeduct selectSkuIdAndQuantity(@Param("txId") String txId,@Param("branchId") Long branchId);

    @Select("SELECT status FROM stock_deduct WHERE tx_id = #{txId} AND branch_id = #{branchId}")
    StockDeduct selectStatus(@Param("txId") String txId,@Param("branchId") Long branchId);
}
