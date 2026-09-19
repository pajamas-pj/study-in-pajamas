package com.pajamas.exam.service;

import com.pajamas.api.dto.exam.QuestionBizDTO;
import com.pajamas.exam.domain.po.QuestionBiz;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 业务与题目关联表 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-09-18
 */
public interface IQuestionBizService extends IService<QuestionBiz> {

    void saveQuestionBizInfoBatch(List<QuestionBizDTO> qbs);

    List<QuestionBizDTO> queryQuestionIdsByBizId(Long bizId);

    List<QuestionBizDTO> queryQuestionIdsByBizIds(List<Long> bizIds);

    Map<Long, Integer> queryQuestionScoresByBizIds(List<Long> bizIds);
}
