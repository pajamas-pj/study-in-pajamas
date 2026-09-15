package com.pajamas.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pajamas.api.dto.course.CategoryBasicDTO;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.exceptions.BizIllegalException;
import com.pajamas.common.utils.BeanUtils;
import com.pajamas.common.utils.CollUtils;
import com.pajamas.course.constants.CourseConstants;
import com.pajamas.course.constants.CourseErrorInfo;
import com.pajamas.course.domain.dto.CategoryIdsDTO;
import com.pajamas.course.domain.dto.CategorySaveDTO;
import com.pajamas.course.domain.dto.CategoryUpdateDTO;
import com.pajamas.course.domain.po.Category;
import com.pajamas.course.domain.vo.CategoryInfoVO;
import com.pajamas.course.enums.CategoryStatus;
import com.pajamas.course.mapper.CategoryMapper;
import com.pajamas.course.service.ICategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 课程分类表 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-23
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Override
    public void saveCategory(CategorySaveDTO categoryAddDTO) {
        //1.校验名称是否重复
        checkSameName(categoryAddDTO.getParentId(), categoryAddDTO.getName(), null);
        int level = 1; //默认一级分类
        if (CourseConstants.CATEGORY_ROOT != categoryAddDTO.getParentId()) {
            //2.1校验父分类是否存在
            Category parentCategory = getById(categoryAddDTO.getParentId());
            if (parentCategory == null) {
                throw new BizIllegalException("父分类不存在");
            }
            //2.2三级课程分类下不能在创建子分类
            if (parentCategory.getLevel() == 3) {
                throw new BizIllegalException("当前不能再创建子分类");
            }
            //2.3分类级别，父分类+1
            level = parentCategory.getLevel() + 1;
        }
        //3.将请求参数转换成VO
        Category category = BeanUtils.copyBean(categoryAddDTO, Category.class);
        category.setLevel(level);
        save(category);
    }

    @Override
    public void deleteCategory(Long id) {
        // 1. 查询当前分类是否存在
        Category category = getById(id);
        if (category == null) {
            throw new BadRequestException("分类不存在");
        }
        // 2. 递归收集所有要删除的分类id
        List<Long> allIds = new ArrayList<>();
        // 2.1加入当前分类id
        allIds.add(id);
        // 2.2递归查询所有子分类id并加入
        collectAllChildIds(id, allIds);
        // 3. 批量逻辑删除
        removeByIds(allIds);
    }

    @Override
    public void updateCategory(CategoryUpdateDTO categoryUpdateDTO) {
        //1.校验分类是否存在
        Category category = getById(categoryUpdateDTO.getId());
        if (category == null) {
            throw new BadRequestException("分类不存在");
        }
        //2.校验是否有同名分类
        checkSameName(category.getParentId(), categoryUpdateDTO.getName(), categoryUpdateDTO.getId());
        //3.更新
        updateById(BeanUtils.copyBean(categoryUpdateDTO,Category.class));
    }
    @Override
    public List<CategoryBasicDTO> getAll() {
        //1.查询数据
        List<Category> list = lambdaQuery()
                .eq(Category::getDeleted, 0)
                .eq(Category::getStatus, CategoryStatus.NORMAL)
                .list();
        if (CollUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        return BeanUtils.copyList(list, CategoryBasicDTO.class);
    }

    @Override
    public CategoryInfoVO getCategory(Long id) {
        //1.查询分类
        Category category = getById(id);
        if(category == null){
            throw new BadRequestException("分类不存在");
        }
        CategoryInfoVO vo = BeanUtils.copyBean(category, CategoryInfoVO.class);
        //2.查询父分类名称
        //2.1如果是二级分类
        if (category.getLevel() == 2) {
            Category parentCategory = getById(category.getParentId());
            if (parentCategory != null) {
                vo.setFirstCategoryName(parentCategory.getName());
            }
        }
        //2.2如果是三级分类
        else if (category.getLevel() == 3) {
            Category parentCategory = getById(category.getParentId());
            if (parentCategory != null) {
                vo.setSecondCategoryName(parentCategory.getName());
                Category parentCategory2 = getById(parentCategory.getParentId());
                if (parentCategory2 != null) {
                    vo.setFirstCategoryName(parentCategory2.getName());
                }
            }
        }
        return vo;
    }

    @Override
    public CategoryIdsDTO getCategoryIdsByThirdId(Long thirdCateId) {
        //1.查询三级课程分类
        Category category = getById(thirdCateId);
        if(category == null || category.getStatus() != CategoryStatus.NORMAL || category.getDeleted() != 0){
            throw new BizIllegalException(CourseErrorInfo.Msg.COURSE_CATEGORY_NOT_FOUND);
        }
        //2.查询二级课程分类
        Category secondCategory = getById(category.getParentId());
        if(secondCategory == null || secondCategory.getStatus() != CategoryStatus.NORMAL || secondCategory.getDeleted() != 0){
            throw new BizIllegalException(CourseErrorInfo.Msg.COURSE_CATEGORY_NOT_FOUND);
        }
        //3.查询一级课程分类
        Category firstCategory = getById(secondCategory.getParentId());
        if(firstCategory == null || firstCategory.getStatus() != CategoryStatus.NORMAL || firstCategory.getDeleted() != 0){
            throw new BizIllegalException(CourseErrorInfo.Msg.COURSE_CATEGORY_NOT_FOUND);
        }
        //3.封装数据并返回
        CategoryIdsDTO ids = new CategoryIdsDTO();
        ids.setFirstCateId(firstCategory.getId());
        ids.setSecondCateId(secondCategory.getId());
        ids.setThirdCateId(category.getId());
        return ids;
    }

    /**
     * 递归查询所有子分类id
     */
    private void collectAllChildIds(Long parentId, List<Long> idList) {
        List<Category> children = lambdaQuery()
                .eq(Category::getParentId, parentId)
                .eq(Category::getDeleted, 0)
                .eq(Category::getStatus, CategoryStatus.NORMAL)
                .list();
        for (Category child : children) {
            idList.add(child.getId());
            collectAllChildIds(child.getId(), idList);
        }
    }
    /**
     * 新增或更新时校验是否有同名的分类
     *
     */
    private void checkSameName(Long parentId, String name, Long currentId) {
        //1.同名查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(Category::getName, name)
                .eq(Category::getDeleted, 0)
                .eq(Category::getStatus, CategoryStatus.NORMAL)
                .and(wrapper -> wrapper
                        //1.1同一个父分类有同名子类
                        .eq(Category::getParentId, parentId)  // 同父分类的子分类
                        .or()
                        //1.2父分类本身有同名
                        .eq(Category::getId, parentId)        // 或者父分类本身
                );
        //2.统计符合上述条件的分类列表
        List<Category> categories = list(queryWrapper);
        if (CollUtils.isEmpty(categories)){
            return;
        }
        //3.新增情况下，有同名的分类
        if (currentId == null) {
            throw new BizIllegalException(CourseErrorInfo.Msg.CATEGORY_SAME_NAME);
        }
        //4.更新情况下出现同名，需要判断是否是当前分类的名称
        if (!Objects.equals(categories.get(0).getId(), currentId)) {
            throw new BizIllegalException(CourseErrorInfo.Msg.CATEGORY_SAME_NAME);
        }
    }
}
