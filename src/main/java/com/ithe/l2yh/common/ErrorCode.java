package com.ithe.l2yh.common;

import lombok.Getter;

/**
 * 自定义错误码
 *
 * @author <a href="https://github.com/HUABAI">ithe</a>
 * @from
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"), // 成功状态码和消息
    PARAMS_ERROR(40000, "请求参数错误"), // 请求参数错误状态码和消息
    NOT_LOGIN_ERROR(40100, "未登录"), // 未登录状态码和消息
    NO_AUTH_ERROR(40101, "无权限"), // 无权限状态码和消息
    NOT_FOUND_ERROR(40400, "请求数据不存在"), // 请求数据不存在状态码和消息
    FORBIDDEN_ERROR(40300, "禁止访问"), // 禁止访问状态码和消息
    SYSTEM_ERROR(50000, "系统内部异常"), // 系统内部异常状态码和消息
    OPERATION_ERROR(50001, "操作失败"), // 操作失败状态码和消息
    API_REQUEST_ERROR(50010, "接口调用错误"), // 接口调用错误状态码和消息
    COMPILE_ERROR(40004, "编译错误"), // 编译错误状态码和消息
    JWT_ERROR(40001, "jwt命牌被篡改"); // jwt命牌被篡改状态码和消息


    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
