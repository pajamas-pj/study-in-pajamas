package com.pajamas.course.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "课程商品信息")
public class CourseItemPageVO {
    @ApiModelProperty(value = "课程id", example = "1")
    private Long id;
    @ApiModelProperty(value = "课程名称", example = "Java")
    private String name;
    @ApiModelProperty(value = "课程价格，单位分", example = "32900")
    private Long price;
    @ApiModelProperty(value = "课程封面地址", example = "default-cover-url.jpg")
    private String coverUrl;
    @ApiModelProperty(value = "课程章节数量", example = "25")
    private Integer sections;
//    @ApiModelProperty(value = "课程报名人数（销量）", example = "3920")
//    private Integer sold;
    @ApiModelProperty(value = "课程评价得分，45代表4.5星", example = "35")
    private Integer score;
    @ApiModelProperty(value = "课程状态，1：待上架，2：已上架，3：已下架，4：已完结", example = "1")
    private Integer status;
    @ApiModelProperty("课程发布时间")
    private LocalDateTime publishTime;
    @ApiModelProperty("下架时间")
    private LocalDateTime purchaseEndTime;
}