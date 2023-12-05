package com.cuit.gcsj.model.enums;

import com.cuit.gcsj.common.ErrorCode;
import com.cuit.gcsj.exception.BusinessException;
import lombok.Getter;

/**
 * 队伍状态枚举类
 *
 * @author L
 */
@Getter
public enum MatchTypeEnum {
    ALEX(0, "start.py"),
    XANDER(1, "end.py"),
    ALEXANDER(2, "mary.py");
    // 状态值
    private final int value;
    // 状态文本
    private final String text;

    public  static MatchTypeEnum getEnumByValue(Integer value) {
        if(value == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"过滤方式不能为空！");
        }
        if (value < 0) {
            return null;
        }
        for (MatchTypeEnum statusEnum : MatchTypeEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }
    MatchTypeEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }
}

