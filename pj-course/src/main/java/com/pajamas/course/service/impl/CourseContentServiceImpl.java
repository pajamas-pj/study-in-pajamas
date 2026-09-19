package com.pajamas.course.service.impl;

import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.utils.BeanUtils;
import com.pajamas.course.domain.po.CourseContent;
import com.pajamas.course.domain.vo.CourseContentVO;
import com.pajamas.course.mapper.CourseContentMapper;
import com.pajamas.course.service.ICourseContentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 课程内容详情表 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-26
 */
@Service
public class CourseContentServiceImpl extends ServiceImpl<CourseContentMapper, CourseContent> implements ICourseContentService {
    @Override
    public CourseContentVO getContentByCourseId(Long courseId) {
        CourseContent content = getById(courseId);
        if (content == null) {
            throw new BadRequestException("课程内容不存在");
        }
        return BeanUtils.copyBean(content, CourseContentVO.class);
    }
}
