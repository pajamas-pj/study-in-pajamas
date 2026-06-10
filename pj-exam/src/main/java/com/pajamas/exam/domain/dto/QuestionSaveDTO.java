package com.pajamas.exam.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(description = "题目保存表单")
public class QuestionSaveDTO {

    @ApiModelProperty("题目ID，新增不传，更新传")
    private Long id;

    @ApiModelProperty(value = "题目类型：1-单选题 2-多选题 3-判断题 4-简答题", required = true, example = "1")
    @NotNull(message = "题目类型不能为空")
    private Integer type;

    @ApiModelProperty(value = "题干内容", required = true)
    @NotNull(message = "题干内容不能为空")
    private String content;

    @ApiModelProperty(value = "分值", example = "10")
    private Integer score;

    @ApiModelProperty(value = "难度：1-简单 2-普通 3-困难", example = "1")
    private Integer difficulty;

    @ApiModelProperty("选项列表（选择题时必填）")
    private List<QuestionOptionDTO> options;

    @ApiModelProperty("参考答案（简答题时填写）")
    private String answer;

    @ApiModelProperty("答案解析")
    private String explanation;
}
