package com.pajamas.course.controller;


import com.pajamas.course.domain.vo.CourseContentVO;
import com.pajamas.course.service.ICourseContentService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 课程内容详情表 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-26
 */
@RestController
@RequestMapping("/courses/course-content")
@RequiredArgsConstructor
public class CourseContentController {

    private final ICourseContentService courseContentService;

    @ApiOperation("根据课程id查询课程内容")
    @GetMapping("/{courseId}")
    public CourseContentVO getContentByCourseId(
            @ApiParam("课程id") @PathVariable("courseId") Long courseId) {
        return courseContentService.getContentByCourseId(courseId);
    }
}
