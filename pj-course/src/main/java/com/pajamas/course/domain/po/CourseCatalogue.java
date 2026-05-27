package com.pajamas.course.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 课程目录表（章/节/测试）
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("course_catalogue")
public class CourseCatalogue implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程目录id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 目录名称
     */
    private String name;

    /**
     * 是否支持试看（0-否 1-是）
     */
    private Integer trailer;

    /**
     * 课程id
     */
    private Long courseId;

    /**
     * 目录类型 1-章 2-节 3-测试
     */
    private Integer type;

    /**
     * 所属章id，章默认为0，小节和测试填写章id
     */
    private Long parentCatalogueId;

    /**
     * 媒资id
     */
    private Long mediaId;

    /**
     * 视频id
     */
    private Long videoId;

    /**
     * 视频名称
     */
    private String videoName;

    /**
     * 直播开始时间
     */
    private LocalDateTime livingStartTime;

    /**
     * 直播结束时间
     */
    private LocalDateTime livingEndTime;

    /**
     * 是否支持回放（0-否 1-是）
     */
    private Integer playBack;

    /**
     * 视频时长（秒）
     */
    private Integer mediaDuration;

    /**
     * 用于章节排序
     */
    private Integer index;

    /**
     * 部门id
     */
    private Long depId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private Long creator;

    /**
     * 更新人
     */
    private Long updater;

    /**
     * 逻辑删除（0-未删除 1-已删除）
     */
    private Boolean deleted;

}
