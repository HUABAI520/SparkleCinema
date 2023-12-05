package com.cuit.gcsj.model.enums;

import lombok.Getter;

/**
 * 队伍状态枚举类
 *
 * @author L
 */
@Getter
public enum TeamStatusEnum {
    PUBLIC(0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");
    // 状态值
    private final int value;
    // 状态文本
    private final String text;

    public  static  TeamStatusEnum getEnumByValue(Integer value) {
        if (value < 0) {
            return null;
        }
        for (TeamStatusEnum statusEnum : TeamStatusEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }
}

