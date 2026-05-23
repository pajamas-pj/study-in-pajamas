package com.pajamas.user.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import com.fasterxml.jackson.annotation.JsonValue;
import com.pajamas.common.exceptions.BadRequestException;
import lombok.Getter;

@Getter
public enum UserStatus {
    NORMAL(1, "正常"),
    FROZEN(2, "冻结"),
    ;
    @EnumValue
    @JsonValue
    int value;
    String desc;

    UserStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static UserStatus of(int value) {
        if (value == 1) {
            return NORMAL;
        }
        if (value == 2) {
            return FROZEN;
        }
        throw new BadRequestException("账户状态错误");
    }
}