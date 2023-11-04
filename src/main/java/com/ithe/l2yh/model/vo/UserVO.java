package com.ithe.l2yh.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 * @TableName user
 */

@Data
public class UserVO implements Serializable {
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 性别
     */
    private Integer gender;
    /**
     * 手机号
     */
    private String phone;

    /**
     * l2系统编号
     */
    private String l2Code;

    /**
     * 邮箱
     */
    private String email;

    /**
     *  用户角色
     */
    private String userRole;


    /**
     * 用户状态 0-正常 1-异常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;


    private static final long serialVersionUID = 1L;
}