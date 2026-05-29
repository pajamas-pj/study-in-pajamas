package com.pajamas.course.service.impl;

import com.pajamas.course.domain.po.CatalogueQuestion;
import com.pajamas.course.mapper.CatalogueQuestionMapper;
import com.pajamas.course.service.ICatalogueQuestionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 课程目录与题目关系表 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-28
 */
@Service
public class CatalogueQuestionServiceImpl extends ServiceImpl<CatalogueQuestionMapper, CatalogueQuestion> implements ICatalogueQuestionService {

}
