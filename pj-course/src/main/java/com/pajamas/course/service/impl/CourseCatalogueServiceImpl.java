package com.pajamas.course.service.impl;

import com.pajamas.api.dto.course.CatalogueDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pajamas.common.utils.CollUtils;
import com.pajamas.course.constants.CourseConstants;
import com.pajamas.course.domain.dto.CatalogueMediaSaveDTO;
import com.pajamas.course.domain.dto.CatalogueQuestionSaveDTO;
import com.pajamas.course.domain.dto.CatalogueSaveDTO;
import com.pajamas.course.domain.po.CatalogueQuestion;
import com.pajamas.course.domain.po.Course;
import com.pajamas.course.domain.po.CourseCatalogue;
import com.pajamas.course.mapper.CourseCatalogueMapper;
import com.pajamas.course.mapper.CourseMapper;
import com.pajamas.course.service.ICatalogueQuestionService;
import com.pajamas.course.service.ICourseCatalogueService;
import com.pajamas.course.utils.RedisIdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pajamas.common.exceptions.BadRequestException;
import com.pajamas.common.utils.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseCatalogueServiceImpl extends ServiceImpl<CourseCatalogueMapper, CourseCatalogue> implements ICourseCatalogueService {

    private final RedisIdWorker redisIdWorker;
    private final CourseMapper courseMapper;
    private final ICatalogueQuestionService catalogueQuestionService;

    @Override
    public List<CatalogueDTO> queryCourseCatalogues(Long id, boolean b) {
        return List.of();
    }

    @Override
    @Transactional
    public void saveMedia(Long courseId, List<CatalogueMediaSaveDTO> mediaList) {
        //1. 参数校验
        if (CollUtils.isEmpty(mediaList)) {
            return;
        }
        //2. 更新目录的媒资字段
        List<CourseCatalogue> catalogues = mediaList.stream().map(media -> {
            CourseCatalogue catalogue = getById(media.getId());
            if (catalogue == null || !courseId.equals(catalogue.getCourseId())) {
                throw new BadRequestException("目录不存在");
            }
            catalogue.setMediaId(media.getMediaId());
            catalogue.setVideoName(media.getVideoName());
            catalogue.setTrailer(media.getTrailer());
            catalogue.setMediaDuration(media.getMediaDuration());
            return catalogue;
        }).collect(Collectors.toList());
        updateBatchById(catalogues);
        //3. 重算课程总时长（汇总所有节的时长）
        List<CourseCatalogue> sections = lambdaQuery()
                .eq(CourseCatalogue::getCourseId, courseId)
                .eq(CourseCatalogue::getType, CourseConstants.CataType.SECTION)
                .isNotNull(CourseCatalogue::getMediaDuration)
                .list();
        int totalDuration = sections.stream()
                .mapToInt(CourseCatalogue::getMediaDuration)
                .sum();
        Course course = courseMapper.selectById(courseId);
        if (course != null) {
            course.setMediaDuration(totalDuration);
            courseMapper.updateById(course);
        }
    }

    @Override
    @Transactional
    public void saveQuestion(Long courseId, List<CatalogueQuestionSaveDTO> questionList) {
        //1. 参数校验
        if (CollUtils.isEmpty(questionList)) {
            return;
        }
        //2. 遍历每个目录的题目绑定
        List<CatalogueQuestion> toSave = new ArrayList<>();
        for (CatalogueQuestionSaveDTO dto : questionList) {
            //2.1 校验目录存在且属于该课程
            CourseCatalogue catalogue = getById(dto.getCatalogueId());
            if (catalogue == null || !courseId.equals(catalogue.getCourseId())) {
                throw new BadRequestException("目录不存在");
            }
            //2.2 只允许小节和测试绑定题目
            if (CourseConstants.CataType.CHAPTER == catalogue.getType()) {
                throw new BadRequestException("章不能绑定题目");
            }
            //2.3 清除该目录下旧关系
            catalogueQuestionService.remove(
                    new LambdaQueryWrapper<CatalogueQuestion>()
                            .eq(CatalogueQuestion::getCatalogueId, dto.getCatalogueId())
            );
            //2.4 展开 questionIds 为 PO
            List<Long> questionIds = dto.getQuestionIds();
            if (CollUtils.isNotEmpty(questionIds)) {
                for (int i = 0; i < questionIds.size(); i++) {
                    CatalogueQuestion po = new CatalogueQuestion();
                    po.setCatalogueId(dto.getCatalogueId());
                    po.setQuestionId(questionIds.get(i));
                    po.setCourseId(courseId);
                    po.setSortOrder(i);
                    toSave.add(po);
                }
            }
        }
        //3. 批量入库
        catalogueQuestionService.saveBatch(toSave);
    }

    @Override
    @Transactional
    public void saveCatalogue(Long courseId, List<CatalogueSaveDTO> chapters) {
        //1. 参数校验
        if (chapters == null || CollUtils.isEmpty(chapters)) {
            return;
        }
        //2. 章集合按序号升序排列
        chapters.sort(Comparator.comparingInt(CatalogueSaveDTO::getIndex));
        //3. 查询该课程下已有目录，构建旧数据映射
        List<CourseCatalogue> oldList = lambdaQuery()
                .eq(CourseCatalogue::getCourseId, courseId)
                .list();
        Map<Long, CourseCatalogue> oldMap = oldList.stream()
                .collect(Collectors.toMap(CourseCatalogue::getId, c -> c));
        //4. 遍历章集合，递归构建待保存的PO列表
        //   visitedIds 记录本次流程处理过的目录ID，用于后续比对删除
        Set<Long> visitedIds = new HashSet<>();
        List<CourseCatalogue> toSave = new ArrayList<>();
        for (CatalogueSaveDTO chapter : chapters) {
            CourseCatalogue po;
            Long id = chapter.getId();
            //1. 判断新增还是更新
            if (id == null) {
                //1.1 新增：拷贝DTO属性，用RedisIdWorker生成分布式ID
                po = BeanUtils.copyBean(chapter, CourseCatalogue.class);
                id = redisIdWorker.nextId("catalogue");
                po.setId(id);
                po.setCourseId(courseId);
            } else {
                //1.2 更新：从旧数据中取出PO，只更新前端可修改的字段
                po = oldMap.get(id);
                if (po == null) {
                    throw new BadRequestException("目录不存在");
                }
                po.setName(chapter.getName());
                po.setIndex(chapter.getIndex());
            }
            //2. 覆盖type字段（新增时从dto拷贝后已有值，更新时显式覆盖）
            po.setType(chapter.getType());
            //3. 记录已处理ID，加入待保存列表
            visitedIds.add(po.getId());
            toSave.add(po);
            //4.1 处理章其下的小节/测试
            buildCataloguePo(chapter, courseId, id, oldMap, visitedIds, toSave);
        }
        //5. 物理删除：旧数据中未被本次请求覆盖的目录
        Set<Long> toDelete = new HashSet<>(oldMap.keySet());
        toDelete.removeAll(visitedIds);
        if (!toDelete.isEmpty()) {
            removeByIds(toDelete);
        }
        //6. 批量新增/更新
        saveOrUpdateBatch(toSave);
        //7. 更新课程总节数（小节 + 测试）
        long sectionCount = lambdaQuery()
                .eq(CourseCatalogue::getCourseId, courseId)
                .in(CourseCatalogue::getType, CourseConstants.CataType.SECTION, CourseConstants.CataType.PRATICE)
                .eq(CourseCatalogue::getDeleted, 0)
                .count();
        Course course = courseMapper.selectById(courseId);
        if (course != null) {
            course.setSectionNum((int) sectionCount);
            courseMapper.updateById(course);
        }
    }

    /**
     * 递归构建目录PO
     * @param chapter            前端传入的目录数据
     * @param courseId           课程ID
     * @param parentCatalogueId  所属章ID（章传0，小节和测试传所属章ID）
     * @param oldMap             旧目录映射
     * @param visitedIds         已处理ID集合
     * @param toSave             待保存PO集合
     */
    private void buildCataloguePo(CatalogueSaveDTO chapter, Long courseId, Long parentCatalogueId,
                                  Map<Long, CourseCatalogue> oldMap, Set<Long> visitedIds,
                                  List<CourseCatalogue> toSave) {
        //4. 若为章且有子级，递归保存小节和测试
        if (chapter.getSections() != null && !chapter.getSections().isEmpty()) {
            //4.1 子级按序号升序
            chapter.getSections().sort(Comparator.comparingInt(CatalogueSaveDTO::getIndex));
            for (CatalogueSaveDTO section : chapter.getSections()) {
                //4.2 遍历
                CourseCatalogue po;
                Long id = section.getId();
                //1. 判断新增还是更新
                if (id == null) {
                    //1.1 新增：拷贝DTO属性，用RedisIdWorker生成分布式ID
                    po = BeanUtils.copyBean(section, CourseCatalogue.class);
                    id = redisIdWorker.nextId("catalogue");
                    po.setId(id);
                    po.setCourseId(courseId);
                    po.setParentCatalogueId(parentCatalogueId);
                } else {
                    //1.2 更新：从旧数据中取出PO，只更新前端可修改的字段
                    po = oldMap.get(id);
                    if (po == null) {
                        throw new BadRequestException("目录不存在");
                    }
                    po.setName(section.getName());
                    po.setIndex(section.getIndex());
                }
                //2. 覆盖type字段（新增时从dto拷贝后已有值，更新时显式覆盖）
                po.setType(section.getType());
                //3. 记录已处理ID，加入待保存列表
                visitedIds.add(po.getId());
                toSave.add(po);
            }
        }
    }
}
