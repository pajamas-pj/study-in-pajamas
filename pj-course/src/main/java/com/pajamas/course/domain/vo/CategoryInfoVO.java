package com.pajamas.course.domain.vo;

import com.pajamas.course.enums.CategoryStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CategoryInfoVO {
    @ApiModelProperty("课程分类id")
    private Long id;
    @ApiModelProperty("课程分类名称")
    private String name;
    @ApiModelProperty("状态：1:正常，0:禁用")
    private CategoryStatus status;
    @ApiModelProperty("分类级别，1：一级分类，2：二级分类，3：三级分类")
    private Integer categoryLevel;
    @ApiModelProperty("分类优先级")
    private Integer priority;
    @ApiModelProperty("一级分类名称")
    private String firstCategoryName;
    @ApiModelProperty("二级分类名称")
    private String secondCategoryName;
}
