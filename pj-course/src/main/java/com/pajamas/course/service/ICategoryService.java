package com.pajamas.course.service;

import com.pajamas.api.dto.course.CategoryBasicDTO;
import com.pajamas.course.domain.dto.CategoryIdsDTO;
import com.pajamas.course.domain.dto.CategorySaveDTO;
import com.pajamas.course.domain.dto.CategoryUpdateDTO;
import com.pajamas.course.domain.po.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.course.domain.vo.CategoryInfoVO;

import java.util.List;

/**
 * <p>
 * 课程分类表 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-23
 */
public interface ICategoryService extends IService<Category> {

    void saveCategory(CategorySaveDTO categoryAddDTO);

    void deleteCategory(Long id);

    void updateCategory(CategoryUpdateDTO categoryUpdateDTO);

    List<CategoryBasicDTO> getAll();

    CategoryInfoVO getCategory(Long id);

    CategoryIdsDTO getCategoryIdsByThirdId(Long thirdCateId);
}
