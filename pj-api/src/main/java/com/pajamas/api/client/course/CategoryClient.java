package com.pajamas.api.client.course;

import com.pajamas.api.dto.course.CategoryBasicDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(contextId = "category",value = "course-service",path = "/categories")
public interface CategoryClient {

    /**
     * 获取所有课程分类
     * @return  所有课程分类
     */
    @GetMapping("/getAll")
    List<CategoryBasicDTO> getAll();
}
