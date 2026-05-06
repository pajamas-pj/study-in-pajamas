package com.pajamas.learning.service;

import com.pajamas.learning.domain.po.PointsBoard;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.learning.domain.query.PointsBoardQuery;
import com.pajamas.learning.domain.vo.PointsBoardVO;

import java.util.List;

/**
 * <p>
 * 学霸天梯榜 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-28
 */
public interface IPointsBoardService extends IService<PointsBoard> {

    PointsBoardVO queryPointsBoardBySeason(PointsBoardQuery query);

    List<PointsBoard> queryCurrentBoardList(String key, Integer pageNo, Integer pageSize);

    void createPointsBoardTableBySeason(Integer season);
}
