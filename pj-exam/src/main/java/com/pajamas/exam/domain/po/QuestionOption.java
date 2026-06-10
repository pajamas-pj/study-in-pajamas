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
 * 题目选项表
 * </p>
 *
 * @author lipengjie
 * @since 2026-05-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("question_option")
public class QuestionOption implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 选项ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属题目ID
     */
    private Long questionId;

    /**
     * 选项标签：A、B、C、D
     */
    private String optionLabel;

    /**
     * 选项内容，或"正确""错误"
     */
    private String optionContent;

    /**
     * 是否为正确答案：0-否 1-是
     */
    private Boolean isCorrect;

    /**
     * 选项排序（如A→B→C→D）
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
