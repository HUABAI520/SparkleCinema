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
public enum HdfsUploadEnum {
    MOVIES(0, "movies.csv"),
    RATINGS(1, "ratings.csv"),
    TAGS(2, "tags.csv");
    // 状态值
    private final int value;
    // 状态文本
    private final String text;
    public  static HdfsUploadEnum getEnumByValue(Integer value) {
        if(value == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"上传类型不能为空！");
        }
        if (value < 0) {
            return null;
        }
        for (HdfsUploadEnum statusEnum : HdfsUploadEnum.values()) {
            if (statusEnum.value == value) {
                return statusEnum;
            }
        }
        return null;
    }

    HdfsUploadEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }
}

