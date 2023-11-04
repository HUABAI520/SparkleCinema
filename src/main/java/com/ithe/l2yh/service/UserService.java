package com.ithe.l2yh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ithe.l2yh.model.entity.User;
import com.ithe.l2yh.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author L
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2023-10-30 15:37:58
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户输入的密码
     * @param checkPassword 用户输入的确认密码
     * @return 返回用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword,String l2Code);


    /**
     * 用户登录
     *
     * @param userAccount  账户
     * @param userPassword 密码
     * @return 脱敏后的用户信息
     */
    UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 根据User对象获取UserVO对象
     *
     * @param user 用户对象
     * @return UserVO对象
     */
    UserVO getUserVO(User user);


    /**
     * 获取登录用户信息
     *
     * @param request HTTP请求对象
     * @return 登录用户对象
     */
    UserVO getLoginUser(HttpServletRequest request);


    /**
     * 用户登出方法
     *
     * @param request HTTP请求对象
     * @return 如果用户成功登出返回true，否则返回false
     */
    boolean userLogout(HttpServletRequest request);

}
