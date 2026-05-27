package com.pajamas.course.controller;


import com.pajamas.api.dto.course.CategoryBasicDTO;
import com.pajamas.course.domain.dto.CategorySaveDTO;
import com.pajamas.course.domain.dto.CategoryUpdateDTO;
import com.pajamas.course.domain.vo.CategoryInfoVO;
import com.pajamas.course.service.ICategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 课程分类表 前端控制器
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-23
 */
@Api(tags = "课程分类相关接口")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final ICategoryService categoryService;
    @PostMapping("/save")
    @ApiOperation("新增课程分类")
    public void addCategory(@Valid @RequestBody CategorySaveDTO categoryAddDTO) {
        categoryService.saveCategory(categoryAddDTO);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除分类信息")
    @ApiImplicitParam(name = "分类id")
    public void deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
    }

    @PutMapping("/update")
    @ApiOperation("更新课程分类")
    public void updateCategory(@Validated @RequestBody CategoryUpdateDTO categoryUpdateDTO) {
        categoryService.updateCategory(categoryUpdateDTO);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询课程分类信息")
    @ApiImplicitParam(name = "id", value = "分类id")
    public CategoryInfoVO getCategory(@PathVariable("id") Long id) {
        return categoryService.getCategory(id);
    }

    @GetMapping("/getAll")
    @ApiOperation("获取所有课程分类")
    public List<CategoryBasicDTO> allOfOneLevel() {
        return categoryService.getAll();
    }


}
