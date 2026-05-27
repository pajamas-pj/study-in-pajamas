package com.pajamas.course.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CategoryIdsDTO {
    @ApiModelProperty("一级分类id")
    private Long firstCateId;
    @ApiModelProperty("二级分类id")
    private Long secondCateId;
    @ApiModelProperty("三级分类id")
    private Long thirdCateId;
}
