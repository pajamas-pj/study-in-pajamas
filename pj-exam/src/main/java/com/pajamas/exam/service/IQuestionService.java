package com.pajamas.exam.service;

import com.pajamas.api.dto.exam.QuestionDTO;
import com.pajamas.exam.domain.dto.QuestionSaveDTO;
import com.pajamas.exam.domain.po.Question;
import com.pajamas.exam.domain.vo.QuestionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IQuestionService extends IService<Question> {

    void saveQuestion(QuestionSaveDTO dto);

    void updateQuestion(QuestionSaveDTO dto);

    QuestionVO getQuestionVO(Long id);

    void deleteQuestion(Long id);

    List<QuestionDTO> queryQuestionsByIds(List<Long> ids);

    List<QuestionDTO> queryQuestionByBizId(Long bizId);
}
