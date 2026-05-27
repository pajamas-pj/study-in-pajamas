package com.pajamas.course.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.pajamas.course.enums.CategoryStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 课程分类表
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("category")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程分类id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类id，一级分类父id为0
     */
    private Long parentId;

    /**
     * 分类级别，1,2,3：代表一级分类，二级分类，三级分类
     */
    private Integer level;

    /**
     * 同级目录优先级，数字越小优先级越高，可以重复
     */
    private Integer priority;

    /**
     * 课程分类状态，1：正常，0：禁用
     */
    private CategoryStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建者
     */
    private Long creator;

    /**
     * 更新者
     */
    private Long updater;

    /**
     * 逻辑删除标识，0：未删除，1：已删除
     */
    private Integer deleted;


}
