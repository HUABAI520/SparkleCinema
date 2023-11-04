package com.ithe.l2yh.exception;


import com.ithe.l2yh.common.ErrorCode;
import lombok.Getter;

/**
 * 自定义异常类
 *
 * @author <a href="https://github.com/HUABAI">ithe</a>
 * @from
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     * -- GETTER --
     *  获取错误码
     *
     * @return 错误码

     */
    private final int code;

    /**
     * 构造方法
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造方法
     *
     * @param errorCode 错误码
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     * 构造方法
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

}
