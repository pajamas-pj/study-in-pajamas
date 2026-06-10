package com.pajamas.exam.domain.po;

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
 * 测试题目表
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("question")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题目ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 题目类型：1-单选题 2-多选题 3-判断题 4-简答题
     */
    private Integer type;

    /**
     * 题干内容
     */
    private String content;

    /**
     * 分值
     */
    private Integer score;

    /**
     * 难度：1-简单 2-普通 3-困难
     */
    private Integer difficulty;

    /**
     * 答题次数
     */
    private Integer answerTimes;

    /**
     * 正确次数
     */
    private Integer correctTimes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
