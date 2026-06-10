package com.pajamas.stock.service.impl;

import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.stock.domain.dto.StockFormDTO;
import com.pajamas.stock.domain.po.Stock;
import com.pajamas.stock.domain.vo.StockVO;
import com.pajamas.stock.mapper.StockMapper;
import com.pajamas.stock.service.IStockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品库存 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-06-03
 */
@Service
@RequiredArgsConstructor
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements IStockService {

    @Override
    public void createStock(StockFormDTO dto) {
        Stock stock = new Stock();
        stock.setSkuId(dto.getSkuId());
        stock.setQuantity(dto.getQuantity());
        stock.setSold(0);
        stock.setFrozen(0);
        stock.setVersion(0);
        save(stock);
    }

    @Override
    public void updateStock(StockFormDTO dto) {
        Stock stock = getById(dto.getSkuId());
        if (stock == null) {
            throw new BadRequestException("库存不存在");
        }
        stock.setQuantity(dto.getQuantity());
        updateById(stock);
    }

    @Override
    public StockVO getStock(Long skuId) {
        Stock stock = getById(skuId);
        if (stock == null) {
            return null;
        }
        return toVO(stock);
    }

    @Override
    public List<StockVO> listStocks(List<Long> skuIds) {
        return lambdaQuery().in(Stock::getSkuId, skuIds).list()
                .stream().map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteStock(Long skuId) {
        removeById(skuId);
    }

    private StockVO toVO(Stock stock) {
        StockVO vo = new StockVO();
        vo.setSkuId(stock.getSkuId());
        vo.setQuantity(stock.getQuantity());
        vo.setSold(stock.getSold());
        vo.setFrozen(stock.getFrozen());
        vo.setAvailable(stock.getQuantity() - stock.getSold() - stock.getFrozen());
        return vo;
    }
}
