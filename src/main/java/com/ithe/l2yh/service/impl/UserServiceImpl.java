package com.ithe.l2yh.service.impl;

import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ithe.l2yh.common.ErrorCode;
import com.ithe.l2yh.constant.UserConstant;
import com.ithe.l2yh.exception.BusinessException;
import com.ithe.l2yh.mapper.UserMapper;
import com.ithe.l2yh.model.entity.User;
import com.ithe.l2yh.model.enums.UserRoleEnum;
import com.ithe.l2yh.model.vo.UserVO;
import com.ithe.l2yh.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author L
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2023-10-30 15:37:58
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 盐值，混淆密码
     */
    @Resource
    private UserMapper userMapper;
    private static final String SALT = "ithe";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @param l2Code        l2系统编码
     * @return 注册成功返回用户id，否则返回-1
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String l2Code) {
        // 不能包含特殊符号的正则表达式
        String regex = "^[a-zA-Z0-9_]+$";
        // 1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, l2Code)) {
            // todo 统一改为统一异常类
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");

        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (l2Code.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "l2系统编号过长");
        }
        // 使用 Hu 工具进行正则表达式匹配
        boolean isMatch = ReUtil.isMatch(regex, userAccount);
        if (!isMatch) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号包含特殊符号");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper.eq("userAccount", userAccount).or().eq("l2Code", l2Code));
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复或l2编号重复");
        }
        // 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setL2Code(l2Code);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }


    @Override
    public UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 不能包含特殊符号和中文的正则表达式
        String regex = "^[a-zA-Z0-9_]+$";
        // 1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        // 使用 Hu tool 进行正则表达式匹配
        boolean isMatch = ReUtil.isMatch(regex, userAccount);
        if (!isMatch) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号包含特殊符号");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount:{}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 判断用户是否被禁用
        if (user.getUserRole().equals(UserRoleEnum.BAN.getValue()) || user.getUserStatus() == 1) {
            log.info("user login failed, userAccount is disabled");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户被禁用");
        }
        // 脱敏
        UserVO userVO = this.getUserVO(user);
        // 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, userVO);

        return userVO;
    }


    /**
     * 根据给定的用户对象获取对应的UserVO对象
     *
     * @param user 用户对象
     * @return 对应的UserVO对象
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }


    /**
     * 获取登录用户信息
     * <p>
     * 从请求中的session中获取用户登录状态对象，将其转换为UserVO类型后返回。
     * 如果userVO为空或者userVO的id为空，则返回null。
     *
     * @param request HTTP请求对象
     * @return 用户信息的UserVO对象，若用户未登录则返回null
     */
    @Override
    public UserVO getLoginUser(HttpServletRequest request) {
        // 从session中获取用户登录状态对象
        Object user = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        // 将获取到的对象转换为UserVO类型
        UserVO userVO = (UserVO) user;
        // 如果userVO为空或者userVO的id为空，则返回null
        if (userVO == null || userVO.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 返回userVO对象
        return userVO;
    }

    /**
     * 用户退出
     *
     * @param request HTTP请求对象
     * @return
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 如果登录状态存在
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) != null) {
            // 移除登录状态属性
            request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
            return true;
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR,"退出失败");
    }


}




