package com.workorder.controller.admin;

import com.workorder.dto.PageDTO;
import com.workorder.entity.User;
import com.workorder.exception.Result;
import com.workorder.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/list")
    public Result getUserList(PageDTO pageDTO, @RequestParam(required = false) Integer role) {
        return Result.success(userService.getUserPage(pageDTO, role));
    }

    @PutMapping("/status/{id}")
    public Result updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    @GetMapping("/detail/{id}")
    public Result getUserDetail(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PostMapping("/add")
    public Result addUser(@RequestBody User user) {
        userService.addUser(user);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    public Result updateUser(@RequestBody User user) {
        userService.updateUser(user);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }
}