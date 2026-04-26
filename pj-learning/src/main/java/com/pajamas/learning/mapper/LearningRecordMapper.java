package com.pajamas.learning.mapper;

import com.pajamas.api.dto.IdAndNumDTO;
import com.pajamas.learning.domain.po.LearningRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 学习记录表 Mapper 接口
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-24
 */
public interface LearningRecordMapper extends BaseMapper<LearningRecord> {

    List<IdAndNumDTO> countLearnedSections(@Param("userId") Long userId,@Param("begin") LocalDateTime begin,@Param("end") LocalDateTime end);
}
