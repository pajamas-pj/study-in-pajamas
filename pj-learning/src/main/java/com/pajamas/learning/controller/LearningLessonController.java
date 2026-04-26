package com.pajamas.learning.controller;


import com.pajamas.common.domain.PageDTO;
import com.pajamas.common.domain.PageQuery;
import com.pajamas.learning.domain.dto.LearningPlanDTO;
import com.pajamas.learning.domain.vo.LearningLessonVO;
import com.pajamas.learning.domain.vo.LearningPlanPageVO;
import com.pajamas.learning.service.ILearningLessonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * <p>
 * 学生课程表 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-23
 */
@RestController
@RequestMapping("/learning-lesson")
@RequiredArgsConstructor
@Api(tags = "我的课程相关接口")
public class   LearningLessonController {

    private final ILearningLessonService lessonService;

    @GetMapping("/page")
    @ApiOperation("分页查询课程列表")
    public PageDTO<LearningLessonVO> queryMyLessons(PageQuery query){
        return lessonService.queryMyLessons(query);
    }


    @GetMapping("/now")
    @ApiOperation("查询最近学习的课程")
    public LearningLessonVO queryNowLearningLesson(PageQuery query){
        return lessonService.queryNowLearningLesson();
    }

    @ApiOperation("根据id查询指定课程的学习状态")
    @GetMapping("/{courseId}")
    public LearningLessonVO queryLessonByCourseId(@PathVariable("courseId") Long courseId){
        return lessonService.queryLessonByCourseId(courseId);
    }

    @DeleteMapping("/{courseId}")
    @ApiOperation("删除用户课表中指定课程")
    public void deleteLessonByCourseId(@PathVariable("courseId") Long courseId){
        lessonService.deleteLessonByCourseId(courseId);
    }

    @PostMapping("/plans")
    @ApiOperation("创建学习计划")
    public void  createLearningPlans(@RequestBody  @Valid LearningPlanDTO planDTO){
        lessonService.createLearningPlans(planDTO.getCourseId(),planDTO.getFreq());
    }

    @ApiOperation("查询我的学习计划")
    @GetMapping("/plans")
    public LearningPlanPageVO queryMyPlans(PageQuery query){
        return lessonService.queryMyPlans(query);
    }
}
