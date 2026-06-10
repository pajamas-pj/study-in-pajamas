package com.pajamas.api.client.exam.fallback;

import com.pajamas.api.client.exam.QuestionClient;
import com.pajamas.api.dto.exam.ExamQuestionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collections;
import java.util.List;

@Slf4j

public class QuestionClientFallback implements FallbackFactory<QuestionClient> {
    @Override
    public QuestionClient create(Throwable cause) {
        log.error("调用考试服务出现异常", cause);
        return new QuestionClient() {
            @Override
            public List<ExamQuestionDTO> queryQuestionsByIds(List<Long> ids) {
                return Collections.emptyList();
            }
        };
    }
}
