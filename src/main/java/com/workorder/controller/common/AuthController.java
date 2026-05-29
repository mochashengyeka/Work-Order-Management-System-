package com.workorder.controller.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.workorder.entity.User;
import com.workorder.exception.Result;
import com.workorder.mapper.UserMapper;
import com.workorder.security.JwtTokenProvider;
import com.workorder.security.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;

    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest request) {
        log.info("尝试登录用户: {}", request.getUsername());

        // 1. 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
                        .eq(User::getStatus, 1)
        );

        if (user == null) {
            log.warn("用户 {} 不存在或已被禁用", request.getUsername());
            return Result.error(401, "用户名或密码错误");
        }

        // 2. 直接比较明文密码
        if (!request.getPassword().equals(user.getPassword())) {
            log.warn("用户 {} 密码错误", request.getUsername());
            return Result.error(401, "用户名或密码错误");
        }

        // 3. 生成 Token
        String token = tokenProvider.generateToken(user.getUsername());

        // 4. 更新登录时间
        try {
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
        } catch (Exception e) {
            log.error("更新登录时间失败", e);
        }

        // 5. 构建返回信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("avatar", user.getAvatar());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userInfo);

        log.info("用户 {} 登录成功", request.getUsername());
        return Result.success("登录成功", result);
    }

    @GetMapping("/info")
    public Result getUserInfo() {
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            return Result.error(401, "未登录");
        }
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("avatar", user.getAvatar());
        return Result.success(userInfo);
    }

    @PostMapping("/logout")
    public Result logout() {
        return Result.success("退出成功");
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }
}