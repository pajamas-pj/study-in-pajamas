package com.pajamas.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.pajamas.api.dto.course.CategoryBasicDTO;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.exceptions.BizIllegalException;
import com.pajamas.course.constants.CourseConstants;
import com.pajamas.course.constants.CourseErrorInfo;
import com.pajamas.course.domain.dto.CategorySaveDTO;
import com.pajamas.course.domain.dto.CategoryUpdateDTO;
import com.pajamas.course.domain.po.Category;
import com.pajamas.course.domain.vo.CategoryInfoVO;
import com.pajamas.course.enums.CategoryStatus;
import com.pajamas.course.mapper.CategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CategoryServiceImpl 单元测试
 * <p>
 * 【测试目的】只测 Service 层的业务逻辑（校验、分支、异常），不连接真实数据库。
 * <p>
 * 【Mock 原理】
 * - @Mock CategoryMapper：假的数据库访问层，注入到 ServiceImpl 的 baseMapper。
 * - @Spy CategoryServiceImpl：真实 Service 对象，但可以 stub 继承自 MyBatis-Plus 的方法
 *   （如 getById、save、list、lambdaQuery），避免真正访问数据库。
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Spy
    @InjectMocks
    private CategoryServiceImpl categoryService;

    /**
     * 辅助方法：stub list(wrapper) 返回空列表，表示「没有同名分类」。
     * checkSameName 内部会调用 list 查询同名记录。
     */
    private void mockListEmpty() {
        doReturn(Collections.emptyList()).when(categoryService).list(any(LambdaQueryWrapper.class));
    }

    /**
     * 辅助方法：stub list(wrapper) 返回已有同名分类，用于测试「名称重复」场景。
     */
    private void mockListWithSameName(Long existingId) {
        Category existing = new Category();
        existing.setId(existingId);
        doReturn(List.of(existing)).when(categoryService).list(any(LambdaQueryWrapper.class));
    }

    /**
     * 辅助方法：stub lambdaQuery().eq(...).list() 链式调用。
     * delete 递归查子分类、get,
     * getAll 查询全部正常分类都会用到 lambdaQuery。
     */
    @SuppressWarnings("unchecked")
    private void mockLambdaQueryList(List<Category>... results) {
        LambdaQueryChainWrapper<Category> chainWrapper = mock(LambdaQueryChainWrapper.class);
        doReturn(chainWrapper).when(categoryService).lambdaQuery();
        when(chainWrapper.eq(any(), any())).thenReturn(chainWrapper);
        if (results.length == 0) {
            when(chainWrapper.list()).thenReturn(Collections.emptyList());
            return;
        }
        // 多次 list() 调用时依次返回不同结果（如递归删子分类：先返回子节点，再返回空）
        org.mockito.stubbing.OngoingStubbing<List<Category>> stubbing = when(chainWrapper.list());
        for (List<Category> result : results) {
            stubbing = stubbing.thenReturn(result);
        }
    }

    // ===================== save =====================

    @Test
    void save_rootCategory_success() {
        // Given：新增一级分类（parentId=0），且无同名分类
        CategorySaveDTO dto = new CategorySaveDTO();
        dto.setParentId(CourseConstants.CATEGORY_ROOT);
        dto.setName("IT·互联网");
        dto.setPriority(1);
        mockListEmpty();
        // stub save：不让 Spy 真正写库，只验证是否被调用
        doReturn(true).when(categoryService).save(any(Category.class));

        // When：执行保存
        categoryService.save(dto);

        // Then：应调用 save，且 level 被设为 1（一级分类）
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryService).save(captor.capture());
        assertEquals(1, captor.getValue().getLevel());
        assertEquals("IT·互联网", captor.getValue().getName());
    }

    @Test
    void save_parentNotFound_throwsBizIllegalException() {
        // Given：parentId 非 0，但 getById 查不到父分类
        CategorySaveDTO dto = new CategorySaveDTO();
        dto.setParentId(99L);
        dto.setName("子分类");
        dto.setPriority(1);
        mockListEmpty();
        // stub getById：模拟「父分类不存在」
        doReturn(null).when(categoryService).getById(99L);

        // When & Then：应抛出 BizIllegalException
        BizIllegalException ex = assertThrows(BizIllegalException.class, () -> categoryService.save(dto));
        assertEquals("父分类不存在", ex.getMessage());
    }

    @Test
    void save_parentLevel3_throwsBizIllegalException() {
        // Given：父分类已是三级，不能再建子分类
        CategorySaveDTO dto = new CategorySaveDTO();
        dto.setParentId(3L);
        dto.setName("四级分类");
        dto.setPriority(1);
        mockListEmpty();
        Category parent = new Category();
        parent.setId(3L);
        parent.setLevel(3);
        doReturn(parent).when(categoryService).getById(3L);

        // When & Then
        BizIllegalException ex = assertThrows(BizIllegalException.class, () -> categoryService.save(dto));
        assertEquals("当前不能再创建子分类", ex.getMessage());
    }

    @Test
    void save_underLevel1Parent_levelShouldBe2() {
        // Given：在一级分类下新增二级分类
        CategorySaveDTO dto = new CategorySaveDTO();
        dto.setParentId(1L);
        dto.setName("Java");
        dto.setPriority(1);
        mockListEmpty();
        Category parent = new Category();
        parent.setId(1L);
        parent.setLevel(1);
        doReturn(parent).when(categoryService).getById(1L);
        doReturn(true).when(categoryService).save(any(Category.class));

        // When
        categoryService.save(dto);

        // Then：level = 父分类 level + 1 = 2
        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryService).save(captor.capture());
        assertEquals(2, captor.getValue().getLevel());
    }

    @Test
    void save_sameName_throwsBizIllegalException() {
        // Given：已存在同名分类
        CategorySaveDTO dto = new CategorySaveDTO();
        dto.setParentId(0L);
        dto.setName("重复名称");
        dto.setPriority(1);
        mockListWithSameName(99L);

        // When & Then
        BizIllegalException ex = assertThrows(BizIllegalException.class, () -> categoryService.save(dto));
        assertEquals(CourseErrorInfo.Msg.CATEGORY_SAME_NAME, ex.getMessage());
    }

    // ===================== delete =====================

    @Test
    void delete_notFound_throwsBadRequestException() {
        // Given：要删除的分类不存在
        doReturn(null).when(categoryService).getById(100L);

        // When & Then
        BadRequestException ex = assertThrows(BadRequestException.class, () -> categoryService.delete(100L));
        assertEquals("分类不存在", ex.getMessage());
    }

    @Test
    void delete_withChildren_removeAllIds() {
        // Given：分类存在，且有一个子分类
        Category parent = new Category();
        parent.setId(1L);
        doReturn(parent).when(categoryService).getById(1L);

        Category child = new Category();
        child.setId(2L);
        // 第一次 lambdaQuery 返回子分类，第二次返回空（无更深层子分类）
        mockLambdaQueryList(List.of(child), Collections.emptyList());
        doReturn(true).when(categoryService).removeByIds(anyList());

        // When
        categoryService.delete(1L);

        // Then：removeByIds 应包含父 id 和子 id
        ArgumentCaptor<List<Long>> captor = ArgumentCaptor.forClass(List.class);
        verify(categoryService).removeByIds(captor.capture());
        List<Long> removedIds = captor.getValue();
        assertTrue(removedIds.contains(1L));
        assertTrue(removedIds.contains(2L));
    }

    // ===================== update =====================

    @Test
    void update_notFound_throwsBadRequestException() {
        // Given
        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setId(1L);
        dto.setName("新名称");
        dto.setPriority(1);
        doReturn(null).when(categoryService).getById(1L);

        // When & Then
        BadRequestException ex = assertThrows(BadRequestException.class, () -> categoryService.update(dto));
        assertEquals("分类不存在", ex.getMessage());
    }

    @Test
    void update_success() {
        // Given：分类存在，无同名冲突
        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setId(1L);
        dto.setName("更新后名称");
        dto.setPriority(2);
        Category existing = new Category();
        existing.setId(1L);
        existing.setParentId(0L);
        doReturn(existing).when(categoryService).getById(1L);
        mockListEmpty();
        doReturn(true).when(categoryService).updateById(any(Category.class));

        // When
        categoryService.update(dto);

        // Then：应调用 updateById
        verify(categoryService).updateById(any(Category.class));
    }

    @Test
    void update_sameNameAsOther_throwsBizIllegalException() {
        // Given：新名称与其他分类重复（非当前 id）
        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setId(1L);
        dto.setName("与他人同名");
        dto.setPriority(1);
        Category existing = new Category();
        existing.setId(1L);
        existing.setParentId(0L);
        doReturn(existing).when(categoryService).getById(1L);
        mockListWithSameName(99L); // 同名记录 id=99，与当前 id=1 不同

        // When & Then
        BizIllegalException ex = assertThrows(BizIllegalException.class, () -> categoryService.update(dto));
        assertEquals(CourseErrorInfo.Msg.CATEGORY_SAME_NAME, ex.getMessage());
    }

    // ===================== get =====================

    @Test
    void get_notFound_throwsBadRequestException() {
        doReturn(null).when(categoryService).getById(1L);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> categoryService.get(1L));
        assertEquals("分类不存在", ex.getMessage());
    }

    @Test
    void get_level2_hasFirstCategoryName() {
        // Given：二级分类，父分类为一级
        Category category = new Category();
        category.setId(2L);
        category.setLevel(2);
        category.setParentId(1L);
        category.setName("Java");
        Category parent = new Category();
        parent.setId(1L);
        parent.setName("IT·互联网");
        doReturn(category).when(categoryService).getById(2L);
        doReturn(parent).when(categoryService).getById(1L);

        // When
        CategoryInfoVO vo = categoryService.get(2L);

        // Then
        assertEquals("IT·互联网", vo.getFirstCategoryName());
        assertEquals("Java", vo.getName());
    }

    @Test
    void get_level3_hasFirstAndSecondCategoryName() {
        // Given：三级分类，需要两次 getById 查父级链
        Category category = new Category();
        category.setId(3L);
        category.setLevel(3);
        category.setParentId(2L);
        category.setName("Spring");
        Category second = new Category();
        second.setId(2L);
        second.setParentId(1L);
        second.setName("Java");
        Category first = new Category();
        first.setId(1L);
        first.setName("IT·互联网");
        doReturn(category).when(categoryService).getById(3L);
        doReturn(second).when(categoryService).getById(2L);
        doReturn(first).when(categoryService).getById(1L);

        CategoryInfoVO vo = categoryService.get(3L);

        assertEquals("IT·互联网", vo.getFirstCategoryName());
        assertEquals("Java", vo.getSecondCategoryName());
    }

    // ===================== getAll =====================

    @Test
    void getAll_empty_returnsEmptyList() {
        // Given：lambdaQuery 查不到任何分类
        mockLambdaQueryList(Collections.emptyList());

        // When
        List<CategoryBasicDTO> result = categoryService.getAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAll_hasData_returnsDtoList() {
        // Given
        Category c1 = new Category();
        c1.setId(1L);
        c1.setName("IT");
        c1.setParentId(0L);
        c1.setDeleted(0);
        c1.setStatus(CategoryStatus.NORMAL);
        mockLambdaQueryList(List.of(c1));

        List<CategoryBasicDTO> result = categoryService.getAll();

        assertEquals(1, result.size());
        assertEquals("IT", result.get(0).getName());
        assertEquals(1L, result.get(0).getId());
    }
}
