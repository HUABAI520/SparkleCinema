package com.ithe.l2yh.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 用户id
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 用户密码
     */
    private String userPassword;

    /**
     * 手机号码
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
     * 用户状态 0-正常 1-异常
     */
    private Integer userStatus;

    /**
     *  用户角色
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除  0-未删除 1-删除
     */
    @TableLogic // 逻辑删除注解
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}