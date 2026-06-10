package com.pajamas.promotion.enums;

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
public enum CouponTccStatus implements BaseEnum {
    TRIED(1, "已预扣"),
    CONFIRMED(2, "已确认"),
    CANCELLED(3, "已回滚");
    @EnumValue
    @JsonValue
    private final int value;
    private final String desc;

    private static final Map<Integer, CouponTccStatus> VALUE_MAP = new HashMap<>();
    static {
        for (CouponTccStatus status : CouponTccStatus.values()) {
            VALUE_MAP.put(status.value, status);
        }
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static CouponTccStatus fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        return VALUE_MAP.get(value);
    }
}
