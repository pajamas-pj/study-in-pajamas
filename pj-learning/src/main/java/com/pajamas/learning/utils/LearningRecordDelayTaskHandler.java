package com.pajamas.learning.utils;


import com.pajamas.common.utils.JsonUtils;
import com.pajamas.common.utils.StringUtils;
import com.pajamas.learning.domain.po.LearningLesson;
import com.pajamas.learning.domain.po.LearningRecord;
import com.pajamas.learning.mapper.LearningRecordMapper;
import com.pajamas.learning.service.ILearningLessonService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class  LearningRecordDelayTaskHandler {

    private final StringRedisTemplate redisTemplate;
    private final LearningRecordMapper recordMapper;
    private final ILearningLessonService lessonService;
    private final DelayQueue<DelayTask<RecordTaskData>> queue = new DelayQueue<>();
    private final static String RECORD_KEY_PREFIX = "learning:record:{}";
    private static volatile boolean begin = true;

    @PostConstruct
    public void init(){
        //异步执行
        CompletableFuture.runAsync(this::handleDelayTask);
    }
    @PreDestroy
    public void destroy(){
        begin = false;
        log.debug("延迟任务处理结束");
    }
    public void handleDelayTask(){
        while (begin){
            try {
                //1.获取延迟任务
                DelayTask<RecordTaskData> task = queue.take();
                RecordTaskData data = task.getData();
                //2.查询Redis缓存
                LearningRecord record = readRecordCache(data.getLessonId(), data.getSectionId());
                if (record == null){
                    continue;
                }
                //3.比较数据moment值
                if (!Objects.equals(data.getMoment(), record.getMoment())) {
                    continue;
                }
                //4.一致，更新学习记录和课表
                //4.1更新学习记录
                record.setFinished(null);
                recordMapper.updateById(record);
                //4.2更新课表
                LearningLesson lesson = new LearningLesson();
                lesson.setId(data.getLessonId());
                lesson.setLatestSectionId(record.getSectionId());
                lesson.setLatestLearnTime(LocalDateTime.now());
                lessonService.updateById(lesson);
            } catch (Exception e) {
                log.error("处理延迟任务失败",e);
            }

        }
    }
    public void addLearningRecord(LearningRecord record){
        //1.添加数据到Redis缓存
        writeRecordCache(record);
        //2.提交延迟任务到延迟队列中
        queue.add(new DelayTask<>(new RecordTaskData(record), Duration.ofSeconds(20)));
    }

    public void writeRecordCache(LearningRecord record) {
        log.debug("更新学习记录缓存");
        try {
            //1.数据转换
            String json = JsonUtils.toJsonStr(new RecordCacheData(record));
            //2.写入redis
            String key = StringUtils.format(RECORD_KEY_PREFIX, record.getLessonId());
            redisTemplate.opsForHash().put(key,record.getSectionId().toString(), json);
            //3.添加过期时间
            redisTemplate.expire(key, Duration.ofMillis(1));
        } catch (Exception e) {
            log.error("更新学习记录缓存失败",e);
        }
    }

    public LearningRecord readRecordCache(Long lessonId, Long sectionId) {
        try {
            //1.读取Redis数据
            String key = StringUtils.format(RECORD_KEY_PREFIX, lessonId);
            Object cacheData = redisTemplate.opsForHash().get(key, sectionId.toString());
            if (cacheData == null) {
                return null;
            }
            //2.数据检查和转换
            return  JsonUtils.toBean(cacheData.toString(), LearningRecord.class);
        } catch (Exception e) {
            log.error("读取缓存失败",e);
            return null;
        }
    }

    public void cleanRecordCache(Long lessonId, Long sectionId){
        String key = StringUtils.format(RECORD_KEY_PREFIX, lessonId);
        redisTemplate.opsForHash().delete(key, sectionId.toString());
    }

    @Data
    @NoArgsConstructor
    private static class RecordCacheData{
        private Long id;
        private Integer moment;
        private Boolean finished;

        public RecordCacheData(LearningRecord record) {
            this.id = record.getId();
            this.moment = record.getMoment();
            this.finished = record.getFinished();
        }
    }
    @Data
    @NoArgsConstructor
    private static class RecordTaskData{
        private Long lessonId;
        private Long sectionId;
        private Integer moment;

        public RecordTaskData(LearningRecord record) {
            this.lessonId = record.getLessonId();
            this.sectionId = record.getSectionId();
            this.moment = record.getMoment();
        }
    }

}
