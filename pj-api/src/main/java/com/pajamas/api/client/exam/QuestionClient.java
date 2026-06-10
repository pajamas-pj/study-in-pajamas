package com.pajamas.api.client.exam;

import com.pajamas.api.client.exam.fallback.QuestionClientFallback;
import com.pajamas.api.dto.exam.ExamQuestionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "question", value = "exam-service", path = "questions",
        fallbackFactory = QuestionClientFallback.class)
public interface QuestionClient {

    /**
     * 根据题目ID集合批量查询题目（含选项）
     *
     * @param ids 题目ID集合
     * @return 题目列表
     */
    @GetMapping("/batch")
    List<ExamQuestionDTO> queryQuestionsByIds(@RequestParam("ids") List<Long> ids);
}
