package com.pajamas.api.client.course;

import com.pajamas.api.dto.course.CourseSimpleInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "course-service")
public interface CourseClient {

    /**
     * 根据课程id集合查询课程简单信息
     *
     * @param ids id集合
     * @return 课程简单信息的列表
     */
    @GetMapping("/courses/simpleInfo/list")
    List<CourseSimpleInfoDTO> getSimpleInfoList(@RequestParam("ids") Iterable<Long> ids);
}
