package com.pajamas.course.controller;


import com.pajamas.course.service.ICourseCatalogueService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 课程目录表（章/节/测试） 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-25
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/catalogues")
public class CourseCatalogueController {

    private final ICourseCatalogueService courseCatalogueService;

    @ApiOperation("根据课程id查询章节集合")
    @GetMapping("/category/list/{id}")
    @ApiImplicitParam(name = "id", value = "课程id")
    public List<CataSimpleInfoVO> catasIndexList(@PathVariable("id") Long id) {
        return courseCatalogueService.getCatasIndexList(id);
    }


}
