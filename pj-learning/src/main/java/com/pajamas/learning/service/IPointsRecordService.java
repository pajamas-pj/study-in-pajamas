package com.pajamas.learning.service;

import com.pajamas.learning.domain.po.PointsRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.learning.domain.vo.PointsStatisticsVO;
import com.pajamas.learning.enums.PointsRecordType;

import java.util.List;

/**
 * <p>
 * 学习积分记录，每个月底清零 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-28
 */
public interface IPointsRecordService extends IService<PointsRecord> {

    void addPointsRecord(Long userId, int points, PointsRecordType type);

    List<PointsStatisticsVO> queryMyPointsToday();
}
