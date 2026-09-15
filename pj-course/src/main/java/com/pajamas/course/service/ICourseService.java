package com.pajamas.course.service;

import com.pajamas.api.dto.course.CourseFullInfoDTO;
import com.pajamas.api.dto.course.CourseSimpleInfoDTO;
import com.pajamas.common.domain.PageDTO;
import com.pajamas.course.domain.dto.CourseSaveDTO;
import com.pajamas.course.domain.dto.CourseSimpleInfoListDTO;
import com.pajamas.course.domain.po.Course;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pajamas.course.domain.query.CoursePageQuery;
import com.pajamas.course.domain.vo.CourseBaseInfoVO;
import com.pajamas.course.domain.vo.CourseItemPageVO;
import com.pajamas.course.domain.vo.CourseSaveVO;

import java.util.List;

/**
 * <p>
 * 课程表 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-25
 */
public interface ICourseService extends IService<Course> {

    CourseSaveVO save(CourseSaveDTO courseSaveDTO);

    CourseBaseInfoVO getCourseBaseInfo(Long id);

    void deleteById(Long id);

    void upShelf(Long id);

    void downShelf(Long id);

    List<CourseSimpleInfoDTO> getSimpleInfoList(CourseSimpleInfoListDTO courseSimpleInfoListDTO);

    CourseFullInfoDTO getInfoById(Long id, boolean withCatalogue, boolean withTeachers);

    PageDTO<CourseItemPageVO> getCourses(CoursePageQuery coursePageQuery);
}
