package com.pajamas.course.domain.dto;

import com.pajamas.course.constants.CourseErrorInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@ApiModel(description = "课程基本信息实体")
public class CourseSaveDTO {
    @ApiModelProperty(value = "课程id，新增不传")
    private Long id;
    @ApiModelProperty(value = "课程名称",example = "Java后端")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_NAME_NULL)
    private String name;
    @ApiModelProperty("封面链接url")
    private String coverUrl;
    @ApiModelProperty(value = "三级课程分类id",example = "9")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_CATEGORY_NULL)
    private Long thirdCateId;
    @ApiModelProperty(value = "是否是免费",example = "true")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_FREE_NULL)
    private Boolean free;
    @ApiModelProperty(value = "课程价格",example = "0")
    @Min(value = 0, message = CourseErrorInfo.Msg.COURSE_SAVE_PRICE_NEGATIVE)
    private Integer price;
    @ApiModelProperty(value = "购买周期开始时间",example = "2026-03-25T09:09:09")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_PURCHASE_TIME_NULL)
    private LocalDateTime purchaseStartTime;
    @ApiModelProperty(value = "购买周期结束时间",example = "2028-03-25T09:09:09")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_PURCHASE_TIME_NULL)
    private LocalDateTime purchaseEndTime;
    @ApiModelProperty(value = "课程有效期，不传表示没有期限，其他表示月数")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_DURATION_NULL)
    private Integer validDuration;


    @ApiModelProperty(value = "课程介绍",example = "Java后端课程介绍")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_INTRODUCE_NULL)
    private String courseIntroduce;
    @ApiModelProperty(value = "使用人群",example = "Java后端课程使用人群")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_USE_PEOPLE_NULL)
    private String usePeople;
    @ApiModelProperty(value = "课程详情",example = "Java后端课程详情")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_DETAIL_NULL)
    private String courseDetail;
}
