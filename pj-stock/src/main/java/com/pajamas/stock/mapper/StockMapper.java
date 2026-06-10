package com.pajamas.stock.mapper;

import com.pajamas.stock.domain.po.Stock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 商品库存 Mapper 接口
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-03
 */
public interface StockMapper extends BaseMapper<Stock> {
    // 乐观锁冻结（Try）
    @Update("UPDATE stock SET frozen = frozen + #{quantity}, version = version + 1 " +
            "WHERE sku_id = #{skuId} AND quantity - sold - frozen >= #{quantity} AND version = #{oldVersion}")
    int freezeStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity, @Param("oldVersion") Integer oldVersion);

    // Confirm 扣减库存（使用乐观锁或普通更新均可，因为外层已锁流水，这里用普通更新简化）
    @Update("UPDATE stock SET sold = sold + #{quantity}, frozen = frozen - #{quantity} " +
            "WHERE sku_id = #{skuId} AND frozen >= #{quantity}")
    int confirmStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    // Cancel 释放冻结（普通更新，锁已由流水保证）
    @Update("UPDATE stock SET frozen = frozen - #{quantity} " +
            "WHERE sku_id = #{skuId} AND frozen >= #{quantity}")
    int cancelStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    // 查询库存（不加锁，Try 阶段使用乐观锁时读取版本号）
    @Select("SELECT sku_id, quantity, sold, frozen, version FROM stock WHERE sku_id = #{skuId}")
    Stock selectByIdForTry(@Param("skuId") Long skuId);
}
