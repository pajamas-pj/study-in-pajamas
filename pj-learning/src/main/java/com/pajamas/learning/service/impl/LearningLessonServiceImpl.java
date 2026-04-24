package com.pajamas.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pajamas.api.client.course.CatalogueClient;
import com.pajamas.api.client.course.CourseClient;
import com.pajamas.api.dto.course.CataSimpleInfoDTO;
import com.pajamas.api.dto.course.CourseFullInfoDTO;
import com.pajamas.api.dto.course.CourseSimpleInfoDTO;
import com.pajamas.common.domain.PageDTO;
import com.pajamas.common.domain.PageQuery;
import com.pajamas.common.exception.BadRequestException;
import com.pajamas.common.exception.BizIllegalException;
import com.pajamas.common.utils.BeanUtils;
import com.pajamas.common.utils.CollUtils;
import com.pajamas.common.utils.UserContext;
import com.pajamas.learning.domain.po.LearningLesson;
import com.pajamas.learning.domain.vo.LearningLessonVO;
import com.pajamas.learning.enums.LessonStatus;
import com.pajamas.learning.mapper.LearningLessonMapper;
import com.pajamas.learning.service.ILearningLessonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 学生课程表 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-23
 */
@Service
@RequiredArgsConstructor
public class LearningLessonServiceImpl extends ServiceImpl<LearningLessonMapper, LearningLesson> implements ILearningLessonService {

    private final CourseClient courseClient;

    private final CatalogueClient catalogueClient;

    @Override
    @Transactional
    public void addUserLessons(Long userId, List<Long> courseIds) {
        //1.查询课程信息
        List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(courseIds);
        if(CollUtils.isEmpty(simpleInfoList)){
            log.error("课程不存在,无法添加");
            return ;
        }
        List<LearningLesson> list = new ArrayList<>(simpleInfoList.size());
        //2.封装数据
        for (CourseSimpleInfoDTO info : simpleInfoList) {
            LearningLesson lesson = new LearningLesson();
            //2.1获取过期时间
            Integer validDuration = info.getValidDuration();
            LocalDateTime now = LocalDateTime.now();
            lesson.setCreateTime(now);//写创建时间
            lesson.setExpireTime(now.plusMonths(validDuration));//写过期时间
            list.add(lesson);
        }
        //3.批量新增
        saveBatch(list);
    }
    @Override
    public PageDTO<LearningLessonVO> queryMyLessons(PageQuery query) {
        //1.获得当前用户
        Long userId = UserContext.getUser();
        //2.分页查询
        Page<LearningLesson> page = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .page(query.toMpPage("latest_learn_time", false));
        List<LearningLesson> records = page.getRecords();
        if(CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        //3.查询课程信息
        //3.1 获得课程id
        Set<Long> cIds = records.stream().map(LearningLesson::getCourseId).collect(Collectors.toSet());
        //3.2查询课程信息
        List<CourseSimpleInfoDTO> simpleInfoList = courseClient.getSimpleInfoList(cIds);
        if(CollUtils.isEmpty(simpleInfoList)){
            throw new BadRequestException("课程不存在");
        }
        //3.3课程处理成map
        Map<Long, CourseSimpleInfoDTO> map = simpleInfoList.stream().
                collect(Collectors.toMap(CourseSimpleInfoDTO::getId, c -> c));
        //4.封装VO
        List<LearningLessonVO> list = new ArrayList<>(records.size());
        for (LearningLesson r : records) {
            LearningLessonVO vo = BeanUtils.copyBean(r, LearningLessonVO.class);
            CourseSimpleInfoDTO info = map.get(r.getCourseId());
            vo.setCourseName(info.getName());
            vo.setCourseCoverUrl(info.getCoverUrl());
            vo.setSections(info.getSectionNum());
            list.add(vo);
        }
        return PageDTO.of(page, list);
    }

    @Override
    public LearningLessonVO queryNowLearningLesson() {
        //1.获取登录用户id
        Long userId = UserContext.getUser();
        //2.查询当前正在学习的课程
        // 查询正在学习的课程 select * from xx where user_id = #{userId} AND status = 1 order by latest_learn_time limit 1
        LearningLesson lesson = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getStatus, LessonStatus.LEARNING.getValue())
                .orderByDesc(LearningLesson::getLatestLearnTime)
                .last("limit 1")
                .one();
        if(lesson == null){
            return null;
        }
        //远程调用课程服务 给vo中的课程名 封面 章节数 赋值
        CourseFullInfoDTO cInfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
        if (cInfo == null) {
            throw new BizIllegalException("课程不存在");
        }
        //3.封装VO
        LearningLessonVO vo = BeanUtils.copyBean(lesson, LearningLessonVO.class);
        //3.1课表中的课程数量
        Integer count = lambdaQuery().eq(LearningLesson::getUserId, userId).count();
        vo.setCourseAmount(count);

        //3.2最近一次小结编号
        Long latestSectionId = lesson.getLatestSectionId();
        List<CataSimpleInfoDTO> cataInfos =
                catalogueClient.batchQueryCatalogue(CollUtils.singletonList(latestSectionId));
        vo.setLatestSectionIndex(cataInfos.get(0).getCIndex());
        //3.3最近一次小结名称
        vo.setLatestSectionName(cataInfos.get(0).getName());
        return vo;
    }

    @Override
    public LearningLessonVO queryLessonByCourseId(Long courseId) {
        //1.获取当前登录用户id
        Long userId = UserContext.getUser();
        //2.查询课表
        LearningLesson lesson = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if (lesson == null) {
            return null;
        }
        // po转vo返回
        LearningLessonVO vo = BeanUtils.copyProperties(lesson, LearningLessonVO.class);
        return vo;
    }

    @Override
    public void deleteLessonByCourseId(Long courseId) {
        // 1. 获取当前用户
        Long userId = UserContext.getUser();

        // 2. 删除指定课程
        QueryWrapper<LearningLesson> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("course_id", courseId);
        remove(wrapper);
    }
}
