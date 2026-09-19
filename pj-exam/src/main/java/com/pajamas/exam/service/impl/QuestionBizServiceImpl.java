package com.pajamas.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pajamas.api.dto.exam.QuestionBizDTO;
import com.pajamas.common.utils.BeanUtils;
import com.pajamas.common.utils.CollUtils;
import com.pajamas.exam.domain.po.QuestionBiz;
import com.pajamas.exam.mapper.QuestionBizMapper;
import com.pajamas.exam.service.IQuestionBizService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 业务与题目关联表 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-09-18
 */
@Service
public class QuestionBizServiceImpl extends ServiceImpl<QuestionBizMapper, QuestionBiz> implements IQuestionBizService {

    @Override
    public List<QuestionBizDTO> queryQuestionIdsByBizId(Long bizId) {
        List<QuestionBiz> list = lambdaQuery()
                .eq(QuestionBiz::getBizId, bizId)
                .list();
        return BeanUtils.copyList(list, QuestionBizDTO.class);
    }

    @Override
    public List<QuestionBizDTO> queryQuestionIdsByBizIds(List<Long> bizIds) {
        if (CollUtils.isEmpty(bizIds)) {
            return CollUtils.emptyList();
        }
        List<QuestionBiz> list = lambdaQuery()
                .in(QuestionBiz::getBizId, bizIds)
                .list();
        return BeanUtils.copyList(list, QuestionBizDTO.class);
    }

    @Override
    public Map<Long, Integer> queryQuestionScoresByBizIds(List<Long> bizIds) {

        return Map.of();
    }

    @Override
    @Transactional
    public void saveQuestionBizInfoBatch(List<QuestionBizDTO> qbs) {
        if (CollUtils.isEmpty(qbs)) {
            return;
        }
        // 1.获取业务id
        Set<Long> bizIds = qbs.stream().map(QuestionBizDTO::getBizId).collect(Collectors.toSet());
        // 2.删除旧数据
        remove(new LambdaQueryWrapper<QuestionBiz>().in(QuestionBiz::getBizId, bizIds));
        // 3.插入新数据
        List<QuestionBiz> list = qbs.stream()
                .map(q -> QuestionBiz.of(null, q.getBizId(), q.getQuestionId()))
                .collect(Collectors.toList());
        saveBatch(list);
    }

}
