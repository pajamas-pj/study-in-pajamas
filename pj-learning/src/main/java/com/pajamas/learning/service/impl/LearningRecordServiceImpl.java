package com.pajamas.learning.service.impl;

import com.pajamas.api.client.course.CourseClient;
import com.pajamas.api.dto.course.CourseFullInfoDTO;
import com.pajamas.api.dto.learning.LearningLessonDTO;
import com.pajamas.api.dto.learning.LearningRecordDTO;
import com.pajamas.api.dto.learning.LearningRecordFormDTO;
import com.pajamas.common.exception.BizIllegalException;
import com.pajamas.common.exception.DbException;
import com.pajamas.common.utils.BeanUtils;
import com.pajamas.common.utils.UserContext;
import com.pajamas.learning.domain.po.LearningLesson;
import com.pajamas.learning.domain.po.LearningRecord;
import com.pajamas.learning.enums.LessonStatus;
import com.pajamas.learning.enums.SectionType;
import com.pajamas.learning.mapper.LearningRecordMapper;
import com.pajamas.learning.service.ILearningLessonService;
import com.pajamas.learning.service.ILearningRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pajamas.learning.utils.LearningRecordDelayTaskHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 学习记录表 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-04-24
 */
@Service
@RequiredArgsConstructor
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements ILearningRecordService {

    private final ILearningLessonService lessonService;
    private final CourseClient courseClient;
    private final LearningRecordDelayTaskHandler taskHandler;
    @Override
    public LearningLessonDTO queryLearningRecordByCourse(Long courseId) {
        //1.获取登录用户id
        Long userId = UserContext.getUser();
        //2.查询课表
        LearningLesson lesson = lessonService.lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .eq(LearningLesson::getCourseId, courseId)
                .one();
        if (lesson == null) {
            return null;
        }
        //3.查询学习记录
        List<LearningRecord> recordList = lambdaQuery()
                .eq(LearningRecord::getLessonId, lesson.getId())
                //.eq(LearningRecord::getUserId, userId)// 可以不用传userId lessonId是userId和courseId结合
                .list();
        //4.封装结果
        LearningLessonDTO dto = new LearningLessonDTO();
        dto.setId(lesson.getId());// 课表id
        dto.setLatestSectionId(lesson.getLatestSectionId()); // 最近学习的小节id
        dto.setRecords(BeanUtils.copyList(recordList, LearningRecordDTO.class));
        return dto;
    }

    @Override
    public void addLearningRecord(LearningRecordFormDTO recordDTO) {
        // 1.获取登录用户
        Long userId = UserContext.getUser();
        // 2.处理学习记录
        boolean finished = false;
        if (recordDTO.getSectionType() == SectionType.VIDEO.getValue()) {
            // 2.1.处理视频
            finished = handleVideoRecord(userId, recordDTO);
        }else{
            // 2.2.处理考试
            finished = handleExamRecord(userId, recordDTO);
        }
        if(!finished){
            // 没有新学完的小节，无需更新课表中的学习进度
            return;
        }
        // 3.处理课表数据
        handleLearningLessonsChanges(recordDTO);
    }
    // 处理课表相关数据
    private void handleLearningLessonsChanges(LearningRecordFormDTO recordDTO) {
        // 1.查询课表
        LearningLesson lesson = lessonService.getById(recordDTO.getLessonId());
        if (lesson == null) {
            throw new BizIllegalException("课程不存在，无法更新数据！");
        }
        // 2.判断是否有新的完成小节
        boolean allLearned = false;
        // 3.如果有新完成的小节，则需要查询课程数据
        CourseFullInfoDTO cInfo = courseClient.getCourseInfoById(lesson.getCourseId(), false, false);
        if (cInfo == null) {
            throw new BizIllegalException("课程不存在，无法更新数据！");
        }
        // 4.比较课程是否全部学完：已学习小节 >= 课程总小节
        allLearned = lesson.getLearnedSections() + 1 >= cInfo.getSectionNum();
        // 5.更新课表
        lessonService.lambdaUpdate()
                .set(lesson.getLearnedSections() == 0, LearningLesson::getStatus, LessonStatus.LEARNING.getValue())
                .set(allLearned, LearningLesson::getStatus, LessonStatus.FINISHED.getValue())
                .setSql("learned_sections = learned_sections + 1")
                .eq(LearningLesson::getId, lesson.getId())
                .update();
    }
    // 提交视频播放记录
    private boolean handleVideoRecord(Long userId, LearningRecordFormDTO recordDTO) {
        // 1.查询旧的学习记录
        LearningRecord old = queryOldRecord(recordDTO.getLessonId(), recordDTO.getSectionId());
        // 2.判断是否存在
        if (old == null) {
            // 3.不存在，则新增
            // 3.1.转换PO
            LearningRecord record = BeanUtils.copyBean(recordDTO, LearningRecord.class);
            // 3.2.填充数据
            record.setUserId(userId);
            // 3.3.写入数据库
            boolean success = save(record);
            if (!success) {
                throw new DbException("新增学习记录失败！");
            }
            return false;
        }
        //4.存在 更新学习记录 learning_record 更新字段 moment
        //4.1判断是否是第一次学完
        boolean finished = !old.getFinished() && recordDTO.getMoment() * 2 >= recordDTO.getDuration();
        if(!finished){
            LearningRecord record = new LearningRecord();
            record.setMoment(recordDTO.getMoment());
            record.setLessonId(recordDTO.getLessonId());
            record.setSectionId(recordDTO.getSectionId());
            record.setId(old.getId());
            record.setFinished(finished);
            taskHandler.addLearningRecord(record);
            return false;
        }
        //4.2.更新数据
        boolean success = lambdaUpdate()
                .set(LearningRecord::getMoment, recordDTO.getMoment())
                .set(LearningRecord::getFinished, true)
                .set(LearningRecord::getFinishTime, recordDTO.getCommitTime())
                .eq(LearningRecord::getId, old.getId())
                .update();
        if (!success) {
            throw new DbException("更新视频学习记录失败");
        }
        taskHandler.cleanRecordCache(recordDTO.getLessonId(),recordDTO.getSectionId());
        return true;
    }
    private LearningRecord queryOldRecord(Long lessonId, Long sectionId) {
        // 1.查询缓存
        LearningRecord record = taskHandler.readRecordCache(lessonId, sectionId);
        // 2.如果缓存命中，则返回
        if (record != null) {
            return record;
        }
        // 3.如果缓存未命中，则查询数据库
        record = lambdaQuery()
                .eq(LearningRecord::getLessonId, lessonId)
                .eq(LearningRecord::getSectionId, sectionId)
                .one();
        // 4.写入缓存
        taskHandler.writeRecordCache(record);
        return record;
    }
    // 处理考试记录
    private boolean handleExamRecord(Long userId, LearningRecordFormDTO dto) {
        //1.将DTO转换PO
        LearningRecord record = BeanUtils.copyBean(dto, LearningRecord.class);
        record.setUserId(userId);
        record.setFinished(true);// 提交学习记录 代表本小节已学完
        record.setFinishTime(dto.getCommitTime());
        //2.保存学习记录 learning_record
        boolean result = this.save(record);
        if (!result) {
            throw new DbException("新增考试记录失败");
        }
        return true;
    }
}
