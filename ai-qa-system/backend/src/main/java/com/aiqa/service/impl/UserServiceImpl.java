package com.aiqa.service.impl;

import com.aiqa.common.BusinessException;
import com.aiqa.entity.User;
import com.aiqa.mapper.UserMapper;
import com.aiqa.service.UserService;
import com.aiqa.util.Md5Utils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User register(String username, String password) {
        User existingUser = getByUsername(username);
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        
        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Utils.encrypt(password));
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        return user;
    }

    @Override
    public User login(String username, String password) {
        User user = getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        
        String encryptedPassword = Md5Utils.encrypt(password);
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }
}
