package com.pajamas.course.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.pajamas.common.enums.BaseEnum;
import lombok.Getter;

@Getter
public enum CourseStatus implements BaseEnum {
    WAITING(1, "待上架"),
    ON_SHELF(2, "已上架"),
    OFF_SHELF(3, "下架"),
    FINISHED(4, "已完结");

    @JsonValue
    @EnumValue
    final int value;
    final String desc;

    CourseStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CourseStatus of(Integer value) {
        if (value == null) {
            return null;
        }
        for (CourseStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return null;
    }

}