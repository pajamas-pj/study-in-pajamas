package com.pajamas.learning.controller;


import com.pajamas.common.domain.PageDTO;
import com.pajamas.learning.domain.dto.QuestionFormDTO;
import com.pajamas.learning.domain.query.QuestionPageQuery;
import com.pajamas.learning.domain.vo.QuestionVO;
import com.pajamas.learning.service.IInteractionQuestionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 互动提问的问题表 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-26
 */
@RestController
@RequestMapping("/questions")
@Api(tags = "互动问答相关接口")
@RequiredArgsConstructor
public class InteractionQuestionController {

    private final IInteractionQuestionService questionService;

    @ApiOperation("新增互动问题")
    @PostMapping
    public void saveQuestion(@Valid @RequestBody QuestionFormDTO formDTO){
        questionService.saveQuestion(formDTO);
    }

    @ApiOperation("修改互动问题")
    @PutMapping("/{id}")
    public void changeQuestion(@PathVariable Long id, @Valid @RequestBody QuestionFormDTO formDTO){
        questionService.changeQuestion(id,formDTO);
    }

    @ApiOperation("删除互动问题")
    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable Long id, @Valid @RequestBody QuestionFormDTO formDTO){
        questionService.deleteQuestion(id,formDTO);
    }

    @ApiOperation("分页查询问题")
    @GetMapping("/page")
    public PageDTO<QuestionVO> queryQuestionPage(QuestionPageQuery query){
        return questionService.queryQuestionPage(query);
    }

    @ApiOperation("查询问题详情")
    @GetMapping("/{id}")
    public QuestionVO queryQuestionPage(@PathVariable("id")Long id){
        return questionService.queryQuestionById(id);
    }

}
