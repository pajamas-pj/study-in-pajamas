package com.pajamas.stock.service;

import com.pajamas.stock.domain.dto.StockFormDTO;
import com.pajamas.stock.domain.po.Stock;
import com.pajamas.stock.domain.vo.StockVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 商品库存 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-03
 */
public interface IStockService extends IService<Stock> {

    void createStock(StockFormDTO dto);

    void updateStock(StockFormDTO dto);

    StockVO getStock(Long skuId);

    List<StockVO> listStocks(List<Long> skuIds);

    void deleteStock(Long skuId);
}
