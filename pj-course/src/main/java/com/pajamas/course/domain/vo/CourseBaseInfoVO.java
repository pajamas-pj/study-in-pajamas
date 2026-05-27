package com.pajamas.course.domain.vo;

import com.pajamas.course.enums.CourseStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(description = "课程基本信息")
public class CourseBaseInfoVO {
    @ApiModelProperty("课程id")
    private Long id;
    @ApiModelProperty("课程名称")
    private String name;
    @ApiModelProperty("封面url")
    private String coverUrl;
    @ApiModelProperty("一级分类id")
    private Long firstCateId;
    @ApiModelProperty("二级分类id")
    private Long secondCateId;
    @ApiModelProperty("三级分类id")
    private Long thirdCateId;
    @ApiModelProperty("课程分类名称，中间使用/隔开")
    private String cateNames;
    @ApiModelProperty("课时总数量,去掉章，测试，用于编辑回显时该值为空")
    private Integer cateTotalNum;
    @ApiModelProperty("课程评分")
    private Integer score;
    @ApiModelProperty("是否免费")
    private Boolean free;
    @ApiModelProperty("课程价格")
    private Integer price;
    @ApiModelProperty("课程状态，1：待上架，2：已上架，3：下架，4：已完结")
    private CourseStatus status;
    @ApiModelProperty("有效期")
    private Integer validDuration;
    @ApiModelProperty("购买有效期开始")
    private LocalDateTime purchaseStartTime;
    @ApiModelProperty("购买有效期结束")
    private LocalDateTime purchaseEndTime;

    @ApiModelProperty("课程介绍")
    private String introduce;
    @ApiModelProperty("使用人群")
    private String usePeople;
    @ApiModelProperty("详情")
    private String detail;
}