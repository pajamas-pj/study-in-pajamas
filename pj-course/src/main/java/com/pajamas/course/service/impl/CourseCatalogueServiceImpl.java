package com.pajamas.course.service.impl;

import com.pajamas.api.dto.course.CatalogueDTO;
import com.pajamas.course.domain.po.CourseCatalogue;
import com.pajamas.course.mapper.CourseCatalogueMapper;
import com.pajamas.course.service.ICourseCatalogueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 课程目录表（章/节/测试） 服务实现类
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-25
 */
@Service
public class CourseCatalogueServiceImpl extends ServiceImpl<CourseCatalogueMapper, CourseCatalogue> implements ICourseCatalogueService {

    @Override
    public List<CatalogueDTO> queryCourseCatalogues(Long id, boolean b) {
        return List.of();
    }
}
