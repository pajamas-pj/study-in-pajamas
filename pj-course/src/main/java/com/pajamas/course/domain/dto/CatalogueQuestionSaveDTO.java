package com.pajamas.course.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(description = "课程目录题目保存表单")
public class CatalogueQuestionSaveDTO {

    @ApiModelProperty(value = "目录ID(小节、测试)", required = true)
    @NotNull(message = "目录id不能为空")
    private Long catalogueId;

    @ApiModelProperty(value = "题目ID集合，按传入顺序即为排序", required = true)
    @NotNull(message = "题目列表不能为空")
    private List<Long> questionIds;
}
