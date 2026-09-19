package com.pajamas.course.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "课程内容详情")
public class CourseContentVO {

    @ApiModelProperty(value = "课程介绍", example = "本课程从零开始讲解 Spring Cloud Alibaba")
    private String courseIntroduce;

    @ApiModelProperty(value = "适用人群", example = "具备 Java 基础的开发人员")
    private String usePeople;

    @ApiModelProperty(value = "课程详情", example = "<p>课程详情富文本...</p>")
    private String courseDetail;
}