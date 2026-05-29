package com.workorder.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.workorder.entity.User;
import com.workorder.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserMapper userMapper;

    /** 获取当前登录用户 */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        String username = auth.getName();
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getStatus, 1)
        );
    }

    /** 获取当前用户ID */
    public Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /** 获取当前用户角色 */
    public Integer getCurrentUserRole() {
        User user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /** 检查是否为管理员 */
    public boolean isAdmin() {
        return Integer.valueOf(3).equals(getCurrentUserRole());
    }

    /** 检查是否为工程师 */
    public boolean isEngineer() {
        return Integer.valueOf(2).equals(getCurrentUserRole());
    }

    /** 检查是否为报修人 */
    public boolean isReporter() {
        return Integer.valueOf(1).equals(getCurrentUserRole());
    }
}