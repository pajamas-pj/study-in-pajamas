package com.pajamas.course.service;

import com.pajamas.api.dto.course.CatalogueDTO;
import com.pajamas.api.dto.exam.ExamQuestionDTO;
import com.pajamas.course.domain.dto.CatalogueMediaSaveDTO;
import com.pajamas.course.domain.dto.CatalogueQuestionSaveDTO;
import com.pajamas.course.domain.dto.CatalogueSaveDTO;
import com.pajamas.course.domain.po.CourseCatalogue;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 课程目录表（章/节/测试） 服务类
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-25
 */
public interface ICourseCatalogueService extends IService<CourseCatalogue> {

    List<CatalogueDTO> queryCourseCatalogues(Long id, boolean b);

    void saveCatalogue(Long courseId, List<CatalogueSaveDTO> chapters);

    void saveMedia(Long courseId, List<CatalogueMediaSaveDTO> mediaList);

    void saveQuestion(Long courseId, List<CatalogueQuestionSaveDTO> questionList);

    List<ExamQuestionDTO> queryQuestionsByCatalogueId(Long catalogueId);
}
