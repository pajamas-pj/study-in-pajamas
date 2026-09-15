package com.pajamas.course.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.hutool.core.util.StrUtil;
import com.pajamas.api.dto.course.CourseFullInfoDTO;
import com.pajamas.api.dto.course.CourseSimpleInfoDTO;
import com.pajamas.common.domain.PageDTO;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.utils.BeanUtils;
import com.pajamas.common.utils.CollUtils;
import com.pajamas.course.constants.CourseErrorInfo;
import com.pajamas.course.domain.dto.CategoryIdsDTO;
import com.pajamas.course.domain.dto.CourseSaveDTO;
import com.pajamas.course.domain.dto.CourseSimpleInfoListDTO;
import com.pajamas.course.domain.po.Course;
import com.pajamas.course.domain.po.CourseContent;
import com.pajamas.course.domain.query.CoursePageQuery;
import com.pajamas.course.domain.vo.CourseBaseInfoVO;
import com.pajamas.course.domain.vo.CourseItemPageVO;
import com.pajamas.course.domain.vo.CourseSaveVO;
import com.pajamas.course.enums.CourseStatus;
import com.pajamas.course.mapper.CourseMapper;
import com.pajamas.course.service.ICategoryService;
import com.pajamas.course.service.ICourseCatalogueService;
import com.pajamas.course.service.ICourseContentService;
import com.pajamas.course.service.ICourseService;
import com.pajamas.course.utils.RedisIdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 课程表 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    private final ICourseContentService courseContentService;
    private final ICourseCatalogueService courseCatalogueService;
    private final ICategoryService categoryService;
    private final RedisIdWorker IdWorker;

    @Override
    @Transactional
    public CourseSaveVO save(CourseSaveDTO courseSaveDTO) {
        //1.数据校验
        //1.1 课程分类校验
        CategoryIdsDTO category = categoryService.getCategoryIdsByThirdId(courseSaveDTO.getThirdCateId());
        //2.数据转换
        //2.1生成课程id
        long courseId = courseSaveDTO.getId() == null ? IdWorker.nextId("course") : courseSaveDTO.getId();
        //2.2转换课程信息
        Course course = BeanUtils.copyBean(courseSaveDTO, Course.class);
        course.setId(courseId);
        course.setFirstCateId(category.getFirstCateId());
        course.setSecondCateId(category.getSecondCateId());
        course.setThirdCateId(category.getThirdCateId());
        course.setStatus(CourseStatus.WAITING);
        //2.3转换课程内容
        CourseContent courseContent = BeanUtils.copyBean(courseSaveDTO, CourseContent.class);
        courseContent.setId(courseId);
        if(courseSaveDTO.getId() != null){
            updateById(course);
            courseContentService.updateById(courseContent);
            return new CourseSaveVO(courseId);
        }
        //3.保存数据
        //3.1保存课程表
        save(course);
        //3.2保存课程内容表
        courseContentService.save(courseContent);
        return new CourseSaveVO(courseId);
    }

    @Override
    public CourseBaseInfoVO getCourseBaseInfo(Long id) {
        //1.查询课程表
        Course course = getById(id);
        if (course == null) {
            throw new BadRequestException("课程不存在");
        }
        //2.查询课程内容表
        CourseContent courseContent = courseContentService.getById(id);
        //3.封装数据并返回
        CourseBaseInfoVO vo = BeanUtils.copyBean(course, CourseBaseInfoVO.class);
        vo.setIntroduce(courseContent.getCourseIntroduce());
        vo.setUsePeople(courseContent.getUsePeople());
        vo.setDetail(courseContent.getCourseDetail());
        return vo;
    }

    @Override
    public void deleteById(Long id) {
        removeById(id);
        courseContentService.removeById(id);
    }

    @Override
    public void upShelf(Long id) {
        lambdaUpdate().eq(Course::getId, id)
                .set(Course::getStatus, CourseStatus.ON_SHELF)
                .update();
    }

    @Override
    public void downShelf(Long id) {
        lambdaUpdate().eq(Course::getId, id)
                .set(Course::getStatus, CourseStatus.OFF_SHELF)
                .update();
    }

    @Override
    public List<CourseSimpleInfoDTO> getSimpleInfoList(CourseSimpleInfoListDTO courseSimpleInfoListDTO) {
        //1.根据分类id查询课程
        List<Course> courseList = lambdaQuery()
                .in(CollUtils.isNotEmpty(courseSimpleInfoListDTO.getThirdCateIds()),
                        Course::getThirdCateId, courseSimpleInfoListDTO.getThirdCateIds())
                .in(CollUtils.isNotEmpty(courseSimpleInfoListDTO.getIds()),
                        Course::getId, courseSimpleInfoListDTO.getIds())
                .list();
        if (CollUtils.isEmpty(courseList)) {
            return CollUtils.emptyList();
        }
        return BeanUtils.copyToList(courseList, CourseSimpleInfoDTO.class);
    }

    @Override
    public CourseFullInfoDTO getInfoById(Long id, boolean withCatalogue, boolean withTeachers) {
        // 1.查询课程基本信息
        Course course = getById(id);
        if (course == null) {
            throw new BadRequestException(CourseErrorInfo.Msg.COURSE_CHECK_NOT_EXISTS);
        }
        // 2.转换vo
        CourseFullInfoDTO courseFullInfoDTO = BeanUtils.copyBean(course, CourseFullInfoDTO.class);
        // 3.查询目录信息
        if (withCatalogue) {
            courseFullInfoDTO.setChapters(courseCatalogueService.queryCourseCatalogues(id, true));
        }
//        // 4.查询教师信息
//        if (withTeachers) {
//            courseFullInfoDTO.setTeacherIds(courseTeacherService.getTeacherIdOfCourse(id));
//        }
        return courseFullInfoDTO;
    }

    @Override
    public PageDTO<CourseItemPageVO> getCourses(CoursePageQuery coursePageQuery) {
        // 1. 提取查询条件，避免重复调用 getter
        Long firstCateId = coursePageQuery.getFirstCateId();
        Long secondCateId = coursePageQuery.getSecondCateId();
        Long thirdCateId = coursePageQuery.getThirdCateId();
        Boolean free = coursePageQuery.getFree();
        Integer courseType = coursePageQuery.getCourseType();
        String keyword = coursePageQuery.getKeyword();
        Integer status = coursePageQuery.getStatus();
        // 2. 分页查询课程，默认已上架
        Page<Course> page = lambdaQuery()
                .eq(firstCateId != null, Course::getFirstCateId, firstCateId)
                .eq(secondCateId != null, Course::getSecondCateId, secondCateId)
                .eq(thirdCateId != null, Course::getThirdCateId, thirdCateId)
                .eq(free != null, Course::getFree, free)
                .eq(courseType != null, Course::getCourseType, courseType)
                .eq(status != null,Course::getStatus,status)
                .like(StrUtil.isNotEmpty(keyword), Course::getName, keyword)
                .page(coursePageQuery.toMpPage("update_time", false));

        List<Course> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        //3.封装VO
        List<CourseItemPageVO> list = new ArrayList<>(records.size());
        for (Course r : records) {
            CourseItemPageVO vo = BeanUtils.copyBean(r, CourseItemPageVO.class);
            vo.setPrice(r.getPrice() != null ? r.getPrice().longValue() : null);
            vo.setStatus(r.getStatus() != null ? r.getStatus().getValue() : null);
            vo.setSections(r.getSectionNum());
            list.add(vo);
        }
        return PageDTO.of(page, list);
    }
}