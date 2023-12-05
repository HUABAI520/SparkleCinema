package com.cuit.gcsj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cuit.gcsj.annotation.AuthCheck;
import com.cuit.gcsj.common.BaseResponse;
import com.cuit.gcsj.common.ErrorCode;
import com.cuit.gcsj.common.ResultUtils;
import com.cuit.gcsj.exception.BusinessException;
import com.cuit.gcsj.model.dto.user.UserLoginRequest;
import com.cuit.gcsj.model.dto.user.UserQueryRequest;
import com.cuit.gcsj.model.dto.user.UserRegisterRequest;
import com.cuit.gcsj.model.result.LoginResult;
import com.cuit.gcsj.model.vo.UserVO;
import com.cuit.gcsj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.cuit.gcsj.utils.HuaUtils.hasNonEmptyFields;

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
        long userRegister = userService.userRegister(userRegisterRequest);

        return ResultUtils.success(userRegister);
    }

    @PostMapping("/login")
    public BaseResponse<LoginResult> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        UserVO userVO = userService.userLogin(userAccount, userPassword, request);
        if(userVO == null){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"登录失败！");
        }
        LoginResult loginResult = new LoginResult();
        loginResult.setStatus("ok");
        return ResultUtils.success(loginResult);
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

    @PostMapping("/search")
    public BaseResponse<Page<UserVO>> searchUsers(@RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Page<UserVO> userVOList = userService.searchUsers(userQueryRequest,request);
        // 脱敏
        return ResultUtils.success(userVOList);

    }
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserVO userVO, HttpServletRequest request) {
        if (userVO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息为空");
        }
        // 除了id必须要有更新字段传递
        if (!hasNonEmptyFields(userVO)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无更新字段！！！");
        }
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean r = userService.updateUser(userVO, request);
        return ResultUtils.success(r);
    }

    @AuthCheck(mustRole = "admin") // 必须是管理员才能操作
    @GetMapping("/delete")
    public BaseResponse<Boolean> deleteUsers(long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id错误");
        }
        // 框架自动进行逻辑删除
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }
}
