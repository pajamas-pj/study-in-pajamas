package com.pajamas.course.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import com.pajamas.common.enums.BaseEnum;
import lombok.Getter;

@Getter
public enum CategoryStatus implements BaseEnum {
    NORMAL(1, "正常"),
    DISABLED(0, "禁用")
    ;
    @JsonValue
    @EnumValue

    final int value;
    final String desc;
    CategoryStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CategoryStatus of(Integer value){
        if (value == null) {
            return null;
        }
        for (CategoryStatus status : values()) {
            if (status.equalsValue(value)) {
                return status;
            }
        }
        return null;
    }
}
