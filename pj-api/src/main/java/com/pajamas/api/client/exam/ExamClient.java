package com.pajamas.api.client.exam;

import com.pajamas.api.client.exam.fallback.QuestionClientFallback;
import com.pajamas.api.dto.exam.QuestionBizDTO;
import com.pajamas.api.dto.exam.QuestionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(contextId = "question", value = "exam-service",
        fallbackFactory = QuestionClientFallback.class)
public interface ExamClient {

    /**
     * 根据题目ID集合批量查询题目（含选项）
     * @param ids 题目ID集合
     * @return 题目列表
     */
    @GetMapping("/questions/batch")
    List<QuestionDTO> queryQuestionsByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 根据业务id集合（如小节ID）查询题目ID集合
     * @param bizIds 业务ID集合
     * @return 业务关连
     */
    @GetMapping("/question-biz/biz/list")
    List<QuestionBizDTO> queryQuestionIdsByBizIds(@RequestParam("ids") Iterable<Long> bizIds);

}
