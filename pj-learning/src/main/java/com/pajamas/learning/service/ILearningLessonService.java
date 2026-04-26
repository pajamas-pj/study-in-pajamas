package com.pajamas.learning.service;

import com.pajamas.common.domain.PageDTO;
import com.pajamas.common.domain.PageQuery;
import com.pajamas.learning.domain.po.LearningLesson;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.learning.domain.vo.LearningLessonVO;
import com.pajamas.learning.domain.vo.LearningPlanPageVO;

import java.util.List;

/**
 * <p>
 * 学生课程表 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-23
 */
public interface ILearningLessonService extends IService<LearningLesson> {

    void addUserLessons(Long userId, List<Long> courseIds);

    PageDTO<LearningLessonVO> queryMyLessons(PageQuery query);

    void deleteLessonByCourseId(Long courseId);

    LearningLessonVO queryNowLearningLesson();

    LearningLessonVO queryLessonByCourseId(Long courseId);

    void createLearningPlans(Long courseId, Integer freq);

    LearningPlanPageVO queryMyPlans(PageQuery query);
}
