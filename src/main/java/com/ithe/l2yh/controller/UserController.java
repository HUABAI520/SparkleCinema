package com.ithe.l2yh.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ithe.l2yh.common.BaseResponse;
import com.ithe.l2yh.common.ErrorCode;
import com.ithe.l2yh.common.ResultUtils;
import com.ithe.l2yh.constant.UserConstant;
import com.ithe.l2yh.exception.BusinessException;
import com.ithe.l2yh.model.dto.user.UserLoginRequest;
import com.ithe.l2yh.model.dto.user.UserRegisterRequest;
import com.ithe.l2yh.model.entity.User;
import com.ithe.l2yh.model.vo.UserVO;
import com.ithe.l2yh.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author L
 */
@RestController  // 适用于编写restful风格的api，返回默认为JSON类型
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userName = userRegisterRequest.getUserName();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String l2Code = userRegisterRequest.getL2Code();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, l2Code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        long userRegister = userService.userRegister(userAccount, userPassword, checkPassword, l2Code);

        return ResultUtils.success(userRegister);
    }

    @PostMapping("/login")
    public BaseResponse<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        UserVO userVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(userVO);
    }

    @GetMapping("/get/login")
    public BaseResponse<UserVO> getUserLogin(HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);
        return ResultUtils.success(loginUser);
    }


    /**
     * 用户退出接口
     *
     * @param request 用户请求
     * @return 是否成功退出，成功返回true，否则返回false
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        boolean userLogout = userService.userLogout(request);
        return ResultUtils.success(userLogout);
    }

    @GetMapping("/search")
    public BaseResponse<List<UserVO>> searchUsers(String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);

        List<UserVO> userVOList = userList.stream().map(user -> userService.getUserVO(user)).toList();
        // 脱敏
        return ResultUtils.success(userVOList);

    }

    @DeleteMapping("/delete")
    public BaseResponse<Boolean> deleteUsers(@RequestBody long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id错误");
        }
        // 框架自动进行逻辑删除
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    private boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        UserVO userVO = (UserVO) userObj;
        if (userVO == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        /**
         * 判断用户角色是否为管理员角色
         *
         * @param userVO 用户信息对象
         * @return 如果用户角色等于 UserConstant.ADMIN_ROLE，则返回 true，否则返回 false
         */
        return UserConstant.ADMIN_ROLE.equals(userVO.getUserRole());

    }
}
