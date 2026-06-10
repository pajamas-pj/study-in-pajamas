package com.pajamas.api.dto.exam;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "题目信息（Feign传输）")
public class ExamQuestionDTO {

    @ApiModelProperty("题目ID")
    private Long id;

    @ApiModelProperty("题目类型：1-单选题 2-多选题 3-判断题 4-简答题")
    private Integer type;

    @ApiModelProperty("题干内容")
    private String content;

    @ApiModelProperty("分值")
    private Integer score;

    @ApiModelProperty("难度：1-简单 2-普通 3-困难")
    private Integer difficulty;

    @ApiModelProperty("参考答案（简答题时使用）")
    private String answer;

    @ApiModelProperty("答案解析")
    private String explanation;

    @ApiModelProperty("选项列表")
    private List<ExamQuestionOptionDTO> options;
}
