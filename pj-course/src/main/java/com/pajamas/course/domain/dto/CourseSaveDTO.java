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
    @ApiModelProperty("课程id，新增不传")
    private Long id;
    @ApiModelProperty("课程名称")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_NAME_NULL)
    private String name;
    @ApiModelProperty("封面链接url")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_COVER_URL_NULL)
    private String coverUrl;
    @ApiModelProperty("三级课程分类id")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_CATEGORY_NULL)
    private Long thirdCateId;
    @ApiModelProperty("是否是免费")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_FREE_NULL)
    private Boolean free;
    @ApiModelProperty("课程价格")
    @Min(value = 0, message = CourseErrorInfo.Msg.COURSE_SAVE_PRICE_NEGATIVE)
    private Integer price;
    @ApiModelProperty("购买周期开始时间")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_PURCHASE_TIME_NULL)
    private LocalDateTime purchaseStartTime;
    @ApiModelProperty("购买周期结束时间")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_PURCHASE_TIME_NULL)
    private LocalDateTime purchaseEndTime;
    @ApiModelProperty("课程有效期，不传表示没有期限，其他表示月数")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_DURATION_NULL)
    private Integer validDuration;


    @ApiModelProperty("课程介绍")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_INTRODUCE_NULL)
    private String introduce;
    @ApiModelProperty("使用人群")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_USE_PEOPLE_NULL)
    private String usePeople;
    @ApiModelProperty("课程详情")
    @NotNull(message = CourseErrorInfo.Msg.COURSE_SAVE_DETAIL_NULL)
    private String detail;
}
