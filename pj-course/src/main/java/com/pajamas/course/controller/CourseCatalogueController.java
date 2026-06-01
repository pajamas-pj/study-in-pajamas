package com.pajamas.course.controller;

import com.pajamas.api.dto.exam.ExamQuestionDTO;
import com.pajamas.course.domain.dto.CatalogueMediaSaveDTO;
import com.pajamas.course.domain.dto.CatalogueQuestionSaveDTO;
import com.pajamas.course.domain.dto.CatalogueSaveDTO;
import com.pajamas.course.service.ICourseCatalogueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Api(tags = "课程目录相关接口")
@RequiredArgsConstructor
@RequestMapping("/catalogues")
public class CourseCatalogueController {

    private final ICourseCatalogueService courseCatalogueService;

    @ApiOperation("保存课程目录")
    @PostMapping("/save")
    public void saveCatalogue(@RequestParam Long courseId,
                              @RequestBody List<CatalogueSaveDTO> chapters) {
        courseCatalogueService.saveCatalogue(courseId, chapters);
    }

    @ApiOperation("保存课程目录媒资")
    @PostMapping("/save/media")
    public void saveMedia(@RequestParam Long courseId,
                          @RequestBody List<CatalogueMediaSaveDTO> mediaList) {
        courseCatalogueService.saveMedia(courseId, mediaList);
    }

    @ApiOperation("保存课程目录题目")
    @PostMapping("/save/question")
    public void saveQuestion(@RequestParam Long courseId,
                             @RequestBody List<CatalogueQuestionSaveDTO> questionList) {
        courseCatalogueService.saveQuestion(courseId, questionList);
    }

    @ApiOperation("根据目录ID查询题目列表（含选项）")
    @GetMapping("/{catalogueId}/questions")
    @ApiImplicitParam(name = "catalogueId", value = "目录ID")
    public List<ExamQuestionDTO> queryQuestionsByCatalogueId(@PathVariable Long catalogueId) {
        return courseCatalogueService.queryQuestionsByCatalogueId(catalogueId);
    }

}
