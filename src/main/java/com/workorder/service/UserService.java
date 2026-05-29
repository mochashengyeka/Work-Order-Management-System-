package com.workorder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.workorder.dto.PageDTO;
import com.workorder.dto.RegisterDTO;
import com.workorder.entity.User;
import com.workorder.vo.UserVO;

public interface UserService {
    UserVO register(RegisterDTO dto);
    UserVO getCurrentUser();
    UserVO getUserById(Long id);
    IPage<UserVO> getUserPage(PageDTO pageDTO, Integer role);
    void updateStatus(Long id, Integer status);
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(Long id);
}