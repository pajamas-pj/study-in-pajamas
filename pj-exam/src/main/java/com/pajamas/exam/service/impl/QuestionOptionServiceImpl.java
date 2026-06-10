package com.pajamas.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pajamas.exam.domain.po.QuestionOption;
import com.pajamas.exam.mapper.QuestionOptionMapper;
import com.pajamas.exam.service.IQuestionOptionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionOptionServiceImpl extends ServiceImpl<QuestionOptionMapper, QuestionOption> implements IQuestionOptionService {

    @Override
    public List<QuestionOption> queryByQuestionId(Long questionId) {
        return lambdaQuery()
                .eq(QuestionOption::getQuestionId, questionId)
                .orderByAsc(QuestionOption::getSortOrder)
                .list();
    }

    @Override
    public void deleteByQuestionId(Long questionId) {
        remove(new LambdaQueryWrapper<QuestionOption>()
                .eq(QuestionOption::getQuestionId, questionId));
    }
}
