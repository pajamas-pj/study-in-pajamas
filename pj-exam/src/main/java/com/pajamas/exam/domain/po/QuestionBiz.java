package com.pajamas.exam.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 业务与题目关联表
 * </p>
 *
 * @author lipengjie
 * @since 2026-09-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("question_biz")
@AllArgsConstructor(staticName = "of")
public class QuestionBiz implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务id，要关联问题的某业务id，例如小节id
     */
    private Long bizId;

    /**
     * 问题id
     */
    private Long questionId;


}
