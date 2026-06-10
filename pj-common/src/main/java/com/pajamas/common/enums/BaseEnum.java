package com.pajamas.common.enums;

public interface BaseEnum {
    //私有化get@Getter重写,实现类调用
    int getValue();
    String getDesc();

    default boolean equalsValue(Integer value){
        return value != null && getValue() == value;
    }
}
