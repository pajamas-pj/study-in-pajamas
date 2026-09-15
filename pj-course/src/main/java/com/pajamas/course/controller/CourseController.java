package com.pajamas.course.controller;


import com.pajamas.api.dto.course.CourseFullInfoDTO;
import com.pajamas.api.dto.course.CourseSimpleInfoDTO;
import com.pajamas.common.domain.PageDTO;
import com.pajamas.course.domain.dto.CourseSaveDTO;
import com.pajamas.course.domain.dto.CourseSimpleInfoListDTO;
import com.pajamas.course.domain.query.CoursePageQuery;
import com.pajamas.course.domain.vo.CourseBaseInfoVO;
import com.pajamas.course.domain.vo.CourseItemPageVO;
import com.pajamas.course.domain.vo.CourseSaveVO;
import com.pajamas.course.service.ICourseCatalogueService;
import com.pajamas.course.service.ICourseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 课程表 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-25
 */
@Api(tags = "课程相关接口")
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final ICourseService courseService;
    private final ICourseCatalogueService courseCatalogueService;

    @PostMapping("/baseInfo/save")
    @ApiOperation("保存课程基本信息")
    public CourseSaveVO save(@RequestBody CourseSaveDTO courseSaveDTO) {
        return courseService.save(courseSaveDTO);
    }
    @GetMapping("/baseInfo/{id}")
    @ApiOperation("获取课程基础信息")
    @ApiImplicitParam(name = "id", value = "课程id")
    public CourseBaseInfoVO baseInfo(@PathVariable("id") Long id) {
        return courseService.getCourseBaseInfo(id);
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("课程删除")
    @ApiImplicitParam(name = "id", value = "id")
    public void deleteById(@PathVariable("id") Long id) {
        courseService.deleteById(id);
    }

    @PostMapping("/upShelf/{id}")
    @ApiOperation("课程上架")
    public void upShelf(@PathVariable("id") Long id) {
        courseService.upShelf(id);
    }

    @PostMapping("/downShelf/{id}")
    @ApiOperation("课程下架")
    public void downShelf(@PathVariable("id") Long id) {
        courseService.downShelf(id);
    }

    @ApiOperation("根根三级分类或课程id集合查询课程集合")
    @GetMapping("/simpleInfo/list")
    public List<CourseSimpleInfoDTO> getSimpleInfoList(CourseSimpleInfoListDTO courseSimpleInfoListDTO) {
        return courseService.getSimpleInfoList(courseSimpleInfoListDTO);
    }

    @GetMapping("/fullInfo{id}")
    @ApiOperation("根据课程id获得课程详细信息")
    public CourseFullInfoDTO getCourseInfoById(
            @PathVariable("id") Long id,
            @RequestParam(value = "withCatalogue", required = false) boolean withCatalogue,
            @RequestParam(value = "withTeachers", required = false) boolean withTeachers
    ){
        return courseService.getInfoById(id, withCatalogue, withTeachers);
    }

    @ApiOperation("分页查询课程商品")
    @GetMapping("/page")
    public PageDTO<CourseItemPageVO> getCourses(@Validated CoursePageQuery coursePageQuery) {
        return courseService.getCourses(coursePageQuery);
    }
}
