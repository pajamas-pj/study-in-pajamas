package com.pajamas.learning.mapper;

import com.pajamas.learning.domain.po.LearningLesson;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 学生课程表 Mapper 接口
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-23
 */
@Mapper
public interface LearningLessonMapper extends BaseMapper<LearningLesson> {

}
