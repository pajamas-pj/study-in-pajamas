package com.pajamas.exam.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "题目选项")
public class QuestionOptionDTO {

    @ApiModelProperty("选项ID，新增不传，更新传")
    private Long id;

    @ApiModelProperty(value = "选项标签：A、B、C、D", example = "A")
    private String optionLabel;

    @ApiModelProperty(value = "选项内容")
    private String optionContent;

    @ApiModelProperty(value = "是否为正确答案", example = "true")
    private Boolean isCorrect;

    @ApiModelProperty(value = "选项排序", example = "0")
    private Integer sortOrder;
}
