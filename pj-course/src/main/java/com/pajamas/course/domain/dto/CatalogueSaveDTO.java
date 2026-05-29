package com.pajamas.course.domain.dto;

import com.pajamas.course.constants.CourseErrorInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel(description = "课程目录新增表单")
public class CatalogueSaveDTO {
    @ApiModelProperty("章、节、练习id,新增不传")
    private Long id;
    @ApiModelProperty(value = "目录类型 1-章 2-节 3-测试", required = true, example = "1")
    @NotNull(message = "目录类型不能为空")
    @Min(value = 1, message = "目录类型错误")
    @Max(value = 3, message = "目录类型错误")
    private Integer type;
    @ApiModelProperty(value = "章、节、测试名称", required = true, example = "第一章 课程概述")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_CATAS_SAVE_NAME_NULL)
    @Size(max = 30, message = CourseErrorInfo.Msg.COURSE_CATAS_SAVE_NAME_SIZE)
    private String name;
    @ApiModelProperty(value = "排序序号",example = "1")
    private Integer index;

    @ApiModelProperty("当前章的小节或测试")
    private List<CatalogueSaveDTO> sections;

}
