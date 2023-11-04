package com.ithe.l2yh.service;

import com.ithe.l2yh.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


/**
 * 用户测试类
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAdduser() {
        User user = new User();
        user.setUsername("l2");
        user.setUserAccount("123");
        user.setUserAvatar("");
        user.setGender(0);
        user.setUserPassword("123");
        user.setPhone("123");
        user.setEmail("4123");


        boolean result = userService.save(user);
        System.out.println(user.getId());
        System.out.println(result);
        Assertions.assertTrue(result);
    }
    @Test
    void userRegister() {
    }
}