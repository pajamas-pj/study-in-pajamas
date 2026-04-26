package com.pajamas.learning.controller;


import com.pajamas.api.dto.learning.LearningLessonDTO;
import com.pajamas.api.dto.learning.LearningRecordFormDTO;
import com.pajamas.learning.service.ILearningRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 学习记录表 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-24
 */
@RestController
@RequestMapping("/learning-record")
@Api(tags = "学习记录相关接口")
@RequiredArgsConstructor
public class   LearningRecordController {

    private final ILearningRecordService recordService;

    @GetMapping("/course/{courseId}")
    @ApiOperation("查询指定课程的学习记录")
    public LearningLessonDTO queryLearningRecordByCourse(@PathVariable("courseId") Long courseId) {
        return recordService.queryLearningRecordByCourse(courseId);
    }

    @PostMapping
    @ApiOperation("提交学习记录")
    public void addLearningRecord(@RequestBody LearningRecordFormDTO formDto){
        recordService.addLearningRecord(formDto);
    }
}
