package com.aiqa.controller;

import com.aiqa.common.Result;
import com.aiqa.entity.User;
import com.aiqa.service.UserService;
import com.aiqa.util.JwtUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

        @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody LoginRequest request) {
        User user = userService.register(request.getUsername(), request.getPassword());

        
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());

        return Result.success("注册成功", data);
    }

        @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());

        
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());

        return Result.success("登录成功", data);
    }

        @GetMapping("/info")
    public Result<User> getUserInfo(@RequestAttribute Long userId) {
        User user = userService.getById(userId);
        return Result.success(user);
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }
}
