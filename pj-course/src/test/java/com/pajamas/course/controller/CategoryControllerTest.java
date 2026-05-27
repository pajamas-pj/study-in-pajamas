package com.pajamas.course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pajamas.api.dto.course.CategoryBasicDTO;
import com.pajamas.course.domain.dto.CategorySaveDTO;
import com.pajamas.course.domain.dto.CategoryUpdateDTO;
import com.pajamas.course.domain.vo.CategoryInfoVO;
import com.pajamas.course.enums.CategoryStatus;
import com.pajamas.course.service.ICategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CategoryController 单元测试（Web 切片测试）
 * <p>
 * 【测试目的】只测 Controller 层：URL 映射、HTTP 方法、参数绑定、是否调用了 Service。
 * 不测试 Service 内部的业务逻辑（那是 CategoryServiceImplTest 的职责）。
 * <p>
 * 【@WebMvcTest】只启动 Web 层相关组件，不加载整个 Spring Boot 应用（不连 Nacos、数据库等），启动快。
 * <p>
 * 【@MockBean】在 Spring 测试容器里用「假的」ICategoryService 替换真实 Bean。
 * 与纯 @Mock 的区别：@MockBean 让 Controller 注入的是测试容器里的假 Service。
 * <p>
 * 【MockMvc】模拟 HTTP 请求，像前端调用接口一样发 POST/GET，并断言状态码和 JSON 内容。
 */
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ICategoryService categoryService;

    @Test
    void save_validRequest_success() throws Exception {
        // Given：合法的保存请求 JSON
        CategorySaveDTO dto = new CategorySaveDTO();
        dto.setParentId(0L);
        dto.setName("IT·互联网");
        dto.setPriority(1);

        // When：POST /categories/save
        mockMvc.perform(post("/categories/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                // Then：HTTP 200，且 Controller 调用了 categoryService.save
                .andExpect(status().isOk());

        verify(categoryService).save(any(CategorySaveDTO.class));
    }

    @Test
    void save_nameNull_returnsBadRequest() throws Exception {
        // Given：name 为 null，触发 @Valid 校验失败
        String json = "{\"parentId\":0,\"name\":null,\"priority\":1}";

        // When & Then：应返回 400 Bad Request
        mockMvc.perform(post("/categories/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_success() throws Exception {
        // When：DELETE /categories/1
        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isOk());

        // Then：应调用 delete(1L)
        verify(categoryService).delete(eq(1L));
    }

    @Test
    void update_validRequest_success() throws Exception {
        // Given
        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setId(1L);
        dto.setName("Java");
        dto.setPriority(2);

        // When
        mockMvc.perform(put("/categories/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(categoryService).update(any(CategoryUpdateDTO.class));
    }

    @Test
    void getById_success() throws Exception {
        // Given：Mock Service 返回假数据
        CategoryInfoVO vo = new CategoryInfoVO();
        vo.setId(1L);
        vo.setName("IT·互联网");
        vo.setStatus(CategoryStatus.NORMAL);
        when(categoryService.get(1L)).thenReturn(vo);

        // When & Then
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("IT·互联网"));
    }

    @Test
    void getAll_success() throws Exception {
        // Given
        CategoryBasicDTO dto = new CategoryBasicDTO();
        dto.setId(1L);
        dto.setName("IT");
        dto.setParentId(0L);
        when(categoryService.getAll()).thenReturn(List.of(dto));

        // When & Then
        mockMvc.perform(get("/categories/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("IT"))
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
