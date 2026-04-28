package com.pajamas.learning.service;

import com.pajamas.common.domain.PageDTO;
import com.pajamas.learning.domain.dto.QuestionFormDTO;
import com.pajamas.learning.domain.po.InteractionQuestion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.learning.domain.query.QuestionAdminPageQuery;
import com.pajamas.learning.domain.query.QuestionPageQuery;
import com.pajamas.learning.domain.vo.QuestionAdminVO;
import com.pajamas.learning.domain.vo.QuestionVO;

/**
 * <p>
 * 互动提问的问题表 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-26
 */
public interface IInteractionQuestionService extends IService<InteractionQuestion> {

    void saveQuestion(QuestionFormDTO formDTO);

    void changeQuestion(Long questionId, QuestionFormDTO formDTO);

    void deleteQuestion(Long questionId, QuestionFormDTO formDTO);

    PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query);

    QuestionVO queryQuestionById(Long id);

    PageDTO<QuestionAdminVO> queryQuestionAdminPage(QuestionAdminPageQuery query);
}
