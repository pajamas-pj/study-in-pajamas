package com.pajamas.exam.service;

import com.pajamas.exam.domain.po.QuestionOption;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IQuestionOptionService extends IService<QuestionOption> {

    List<QuestionOption> queryByQuestionId(Long questionId);

    void deleteByQuestionId(Long questionId);
}
