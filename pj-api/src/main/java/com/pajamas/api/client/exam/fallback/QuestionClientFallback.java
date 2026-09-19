package com.pajamas.api.client.exam.fallback;

import com.pajamas.api.client.exam.ExamClient;
import com.pajamas.api.dto.exam.QuestionBizDTO;
import com.pajamas.api.dto.exam.QuestionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collections;
import java.util.List;

@Slf4j

public class QuestionClientFallback implements FallbackFactory<ExamClient> {
    @Override
    public ExamClient create(Throwable cause) {
        log.error("调用考试服务出现异常", cause);
        return new ExamClient() {
            @Override
            public List<QuestionDTO> queryQuestionsByIds(List<Long> ids) {
                return Collections.emptyList();
            }

            @Override
            public List<QuestionBizDTO> queryQuestionIdsByBizIds(Iterable<Long> bizIds) {
                return List.of();
            }

        };
    }
}
