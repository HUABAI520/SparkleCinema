package com.cuit.gcsj.service.impl;


import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cuit.gcsj.common.ErrorCode;
import com.cuit.gcsj.constant.CommonConstant;
import com.cuit.gcsj.constant.UserConstant;
import com.cuit.gcsj.exception.BusinessException;
import com.cuit.gcsj.mapper.UserMapper;
import com.cuit.gcsj.model.dto.user.UserQueryRequest;
import com.cuit.gcsj.model.dto.user.UserRegisterRequest;
import com.cuit.gcsj.model.entity.User;
import com.cuit.gcsj.model.enums.UserRoleEnum;
import com.cuit.gcsj.model.vo.UserVO;
import com.cuit.gcsj.service.UserService;
import com.cuit.gcsj.utils.HuaUtils;
import com.cuit.gcsj.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author L
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2023-10-30 15:37:58
 */
@Service
@Slf4j
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 盐值，混淆密码
     */
    @Resource
    private UserMapper userMapper;
    private static final String SALT = "ithe";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户注册
     *
     * @return 注册成功返回用户id，否则返回-1
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String username = userRegisterRequest.getUsername();

        // 不能包含特殊符号的正则表达式
        String regex = "^[a-zA-Z0-9_]+$";
        // 1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            // todo 统一改为统一异常类
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");

        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
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
        synchronized (userAccount.intern()) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            if (username.isEmpty()) {
                // 如果用户没有填写用户名，则默认生成一个随机用户名
                // 生成uuid8位作为用户名
                username = "computer" + java.util.UUID.randomUUID().toString().substring(0, 8);
            }
            // 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUsername(username);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
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

        return UserVO.objToVo(user);
    }


    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
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
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 将获取到的对象转换为UserVO类型
        UserVO userVO = (UserVO) user;
        // 如果userVO为空或者userVO的id为空，则返回null
        if (userVO.getId() == null) {
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
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "退出失败");
    }
    /**
     * 更新用户信息
     *
     * @return true：更新成功，false：更新失败
     */
    @Override
    public Boolean updateUser(UserVO userVO, HttpServletRequest request) {
        Long userId = userVO.getId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户id不能为空");
        }
        // 判断登录用户修改的用户信息是否是本人或者登录用户是管理员,也可以管理员和用户修改接口分开写
        if (userId.equals(this.getLoginUser(request).getId()) || this.isAdmin(request)) {
            //查询数据库是否存在该用户
            if (this.getById(userVO.getId()) == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
            }
            // 更新用户信息
            User user = new User();
            BeanUtils.copyProperties(userVO, user);
            boolean b = this.updateById(user);
            if (b && userId.equals(this.getLoginUser(request).getId())) {
                UserVO userVoNew = getUserVO(this.getById(userId));
                request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, userVoNew);
            }
            return b;
        } else {
            // 若用户不是本人或者是管理员，则抛出业务异常，表示没有权限修改该用户
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }


    /**
     * 判断用户是否是管理员
     *
     * @param request HTTP请求对象
     * @return 如果用户角色等于 UserConstant.ADMIN_ROLE，则返回 true，否则返回 false
     */
    @Override
    public Boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        UserVO userVO = (UserVO) userObj;
        return userVO != null && UserConstant.ADMIN_ROLE.equals(userVO.getUserRole());
    }

    /**
     * 传入登录用户信息，判断用户是否是管理员
     *
     * @return true：是管理员，false：不是管理员
     */
    @Override
    public Boolean isAdmin(UserVO userVO) {
        return userVO != null && UserConstant.ADMIN_ROLE.equals(userVO.getUserRole());
    }

    @Override
    public Page<UserVO> searchUsers(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        // 没有查数据库
        Page<UserVO> userVOList = getUserVOListPage(userQueryRequest);

        return userVOList;
    }

    /**
     * 获得用户分页列表
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public Page<UserVO> getUserVOListPage(UserQueryRequest userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 没有则查数据库
        Page<User> userPage = this.page(new Page<>(current, size),
                this.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        // 使用 ObjectConverter 工具类，将 User 转换为 UserVO
        List<UserVO> userVO = HuaUtils.convertList(userPage.getRecords(), UserVO.class);
        userVOPage.setRecords(userVO); // 将转换后的 UserVO 对象列表设置到 userVOList 中
        return userVOPage;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUsername();
        String userAccount = userQueryRequest.getUserAccount();
        String profile = userQueryRequest.getProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        Integer gender = userQueryRequest.getGender();
        String phone = userQueryRequest.getPhone();
        Integer userStatus = userQueryRequest.getUserStatus();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq(id != null, "id", id)
                .eq(StringUtils.isNotBlank(userRole), "userRole", userRole)
                .like(StringUtils.isNotBlank(userName), "username", userName)
                .like(StringUtils.isNotBlank(userAccount), "userAccount", userAccount)
                .like(StringUtils.isNotBlank(profile), "profile", profile)
                .eq(gender != null, "gender", gender)
                .eq(StringUtils.isNotBlank(phone), "phone", phone)
                .eq(userStatus != null, "userStatus", userStatus);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}









