package com.pajamas.stock.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.pajamas.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum TCCStatus implements BaseEnum {
    TRIED(1, "已预扣"),
    CONFIRMED(2, "已确认"),
    CANCELLED(3, "已回滚");
    @EnumValue
    @JsonValue
    private final int value;
    private final String desc;

    /*/构造器私有采用@AllArgsConstructor实现
    TCCStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }*/

    //静态缓存
    private static final Map<Integer, TCCStatus> VALUE_MAP = new HashMap<>();
    static {
        for (TCCStatus status : TCCStatus.values()) {
            VALUE_MAP.put(status.value, status);
        }
    }
    //JsonCreator提前判null，免得多加equalsValue方法
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TCCStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        return VALUE_MAP.get(value);
        /*遍历改良
        for (TCCStatus status : values()) {
            if (status.equalsValue(value)) {
                return status;
            }
        }
        return null;*/
    }
    /*安全比较：当前枚举是否与某个数值相等（处理 null）
    public boolean equalsValue(Integer value) {
        return value != null && this.value == value;
    }*/
}
