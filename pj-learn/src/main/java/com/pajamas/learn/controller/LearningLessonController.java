package com.pajamas.learn.controller;


import com.pajamas.common.domain.PageDTO;
import com.pajamas.common.domain.PageQuery;
import com.pajamas.learn.domain.vo.LearningLessonVO;
import com.pajamas.learn.service.ILearningLessonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/{courseId}")
    @ApiOperation("删除用户课表中指定课程")
    public void deleteLessonByCourseId(@PathVariable("courseId") Long courseId){
        lessonService.deleteLessonByCourseId(courseId);
    }

    @GetMapping("/now")
    @ApiOperation("查询当前正在学习的课程")
    public LearningLessonVO queryNowLearningLesson(PageQuery query){
        return lessonService.queryNowLearningLesson();
    }

    @ApiOperation("查询用户课表中指定课程状态")
    @GetMapping("/{courseId}")
    public LearningLessonVO queryLessonByCourseId(@PathVariable("courseId") Long courseId){
        return lessonService.queryLessonByCourseId(courseId);
    }
}
