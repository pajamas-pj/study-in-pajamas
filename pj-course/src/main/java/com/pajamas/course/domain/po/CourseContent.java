package com.pajamas.course.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 课程内容详情表
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("course_content")
public class CourseContent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 课程介绍
     */
    private String courseIntroduce;

    /**
     * 适用人群
     */
    private String usePeople;

    /**
     * 课程详情
     */
    private String courseDetail;

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
     * 逻辑删除，0-未删除，1-已删除
     */
    private Integer deleted;


}
