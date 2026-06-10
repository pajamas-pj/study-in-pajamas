package com.pajamas.api.dto.exam;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "题目选项（Feign传输）")
public class ExamQuestionOptionDTO {

    @ApiModelProperty("选项标签：A、B、C、D")
    private String optionLabel;

    @ApiModelProperty("选项内容")
    private String optionContent;

    @ApiModelProperty("是否为正确答案")
    private Boolean isCorrect;

    @ApiModelProperty("选项排序")
    private Integer sortOrder;
}
