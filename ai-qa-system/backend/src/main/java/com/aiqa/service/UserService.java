package com.aiqa.service;

import com.aiqa.entity.User;

public interface UserService {

    /**
     * 用户注册
     */
    User register(String username, String password);

    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 根据ID查询用户
     */
    User getById(Long id);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);
}
