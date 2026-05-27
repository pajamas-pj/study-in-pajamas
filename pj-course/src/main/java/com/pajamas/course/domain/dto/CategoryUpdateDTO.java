package com.pajamas.course.domain.dto;

import com.pajamas.course.constants.CourseErrorInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@ApiModel(description = "分类信息更新实体")
public class CategoryUpdateDTO {
    @ApiModelProperty(name = "分类id", example = "1")
    @NotNull(message = CourseErrorInfo.Msg.CATEGORY_ID_NOT_NULL)
    private Long id;
    @ApiModelProperty(value = "名称", example = "IT")
    @NotNull(message = CourseErrorInfo.Msg.CATEGORY_UPDATE_NAME_NOT_NULL)
    @Size(max = 15, message = CourseErrorInfo.Msg.CATEGORY_UPDATE_NAME_SIZE)
    private String name;
    @ApiModelProperty(name = "分类序号", example = "2")
    @Max(value = 99, message = CourseErrorInfo.Msg.CATEGORY_UPDATE_INDEX_MAX_MIN)
    @Min(value = 1, message = CourseErrorInfo.Msg.CATEGORY_UPDATE_INDEX_MAX_MIN)
    @NotNull(message = CourseErrorInfo.Msg.CATEGORY_UPDATE_INDEX_NOT_NULL)
    private Integer priority;
};