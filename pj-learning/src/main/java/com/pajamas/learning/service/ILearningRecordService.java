package com.pajamas.learning.service;

import com.pajamas.api.dto.learning.LearningLessonDTO;
import com.pajamas.api.dto.learning.LearningRecordFormDTO;
import com.pajamas.learning.domain.po.LearningRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 学习记录表 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-24
 */
public interface ILearningRecordService extends IService<LearningRecord> {

    LearningLessonDTO queryLearningRecordByCourse(Long courseId);

    void addLearningRecord(LearningRecordFormDTO formDto);
}
