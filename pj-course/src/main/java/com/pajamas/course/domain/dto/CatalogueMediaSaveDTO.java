package com.pajamas.course.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "课程目录媒资保存表单")
public class CatalogueMediaSaveDTO {

    @ApiModelProperty(value = "目录id", required = true)
    @NotNull(message = "目录id不能为空")
    private Long id;

    @ApiModelProperty("媒资id")
    private Long mediaId;

    @ApiModelProperty("视频名称")
    private String videoName;

    @ApiModelProperty("是否支持试看（0-否 1-是）")
    private Integer trailer;

    @ApiModelProperty("视频时长（秒）")
    private Integer mediaDuration;
}
