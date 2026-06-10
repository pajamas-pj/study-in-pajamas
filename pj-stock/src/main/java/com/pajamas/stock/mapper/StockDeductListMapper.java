package com.pajamas.stock.mapper;

import com.pajamas.stock.domain.po.StockDeductList;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * TCC事务记录List Mapper 接口
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-07
 */
public interface StockDeductListMapper extends BaseMapper<StockDeductList> {

    //查询事务记录状态
    @Select("SELECT * FROM stock_deduct_list WHERE tx_id = #{txId} AND branch_id = #{branchId}")
    StockDeductList selectStatus(@Param("txId") String txId, @Param("branchId") Long branchId);

}
