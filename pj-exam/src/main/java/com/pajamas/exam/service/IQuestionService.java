package com.pajamas.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pajamas.api.dto.exam.ExamQuestionDTO;
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

    List<ExamQuestionDTO> queryQuestionsByIds(List<Long> ids);
}
