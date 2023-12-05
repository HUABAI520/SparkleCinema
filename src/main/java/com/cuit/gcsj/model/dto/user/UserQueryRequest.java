package com.cuit.gcsj.model.dto.user;

import com.cuit.gcsj.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author <a href="https://github.com/lihz">ithe,itz</a>
 * @from 
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 简介
     */
    private String profile;

    private Integer gender;

    private String phone;

    /**
     * 用户状态 0-正常 1-异常
     */
    private Integer userStatus;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}