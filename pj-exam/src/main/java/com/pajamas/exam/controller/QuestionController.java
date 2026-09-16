package com.pajamas.exam.controller;

import com.pajamas.api.dto.exam.ExamQuestionDTO;
import com.pajamas.exam.domain.dto.QuestionSaveDTO;
import com.pajamas.exam.domain.vo.QuestionVO;
import com.pajamas.exam.service.IQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "题目管理接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/questions")
public class QuestionController {

    private final IQuestionService questionService;

    @ApiOperation("新增题目")
    @PostMapping("/save")
    public void save(@Valid @RequestBody QuestionSaveDTO dto) {
        questionService.saveQuestion(dto);
    }

    @ApiOperation("更新题目")
    @PutMapping("/update")
    public void update(@Valid @RequestBody QuestionSaveDTO dto) {
        questionService.updateQuestion(dto);
    }

    @ApiOperation("删除题目")
    @DeleteMapping("/{id}")
    @ApiImplicitParam(name = "id", value = "题目ID")
    public void delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
    }

    @ApiOperation("查询题目详情")
    @GetMapping("/{id}")
    @ApiImplicitParam(name = "id", value = "题目ID")
    public QuestionVO get(@PathVariable Long id) {
        return questionService.getQuestionVO(id);
    }

    @ApiOperation("根据题目id集合批量查询题目（含选项）")
    @GetMapping("/batch")
    public List<ExamQuestionDTO> queryQuestionsByIds(@RequestParam("ids") List<Long> ids) {
        return questionService.queryQuestionsByIds(ids);
    }
}
