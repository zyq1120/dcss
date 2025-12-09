package cn.masu.dcs.controller;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.result.R;
import cn.masu.dcs.dto.UserAssignRoleDTO;
import cn.masu.dcs.dto.UserCreateDTO;
import cn.masu.dcs.dto.UserUpdateDTO;
import cn.masu.dcs.service.UserService;
import cn.masu.dcs.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * <p>
 * 同时支持 /api/user 和 /api/users 路径，保证向后兼容
 * </p>
 * @author System
 */
@RestController
@RequestMapping({"/api/user", "/api/users"})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    public R<Long> createUser(@Validated @RequestBody UserCreateDTO dto) {
        Long id = userService.createUser(dto);
        return R.ok("创建成功", id);
    }

    /**
     * 更新用户
     */
    @PutMapping
    public R<Boolean> updateUser(@Validated @RequestBody UserUpdateDTO dto) {
        Boolean result = userService.updateUser(dto);
        return R.ok("更新成功", result);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public R<Boolean> deleteUser(@PathVariable Long id) {
        Boolean result = userService.deleteUser(id);
        return R.ok("删除成功", result);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public R<UserVO> getUserDetail(@PathVariable Long id) {
        UserVO vo = userService.getUserDetail(id);
        return R.ok(vo);
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping("/page")
    public R<PageResult<UserVO>> getUserPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        PageResult<UserVO> pageResult = userService.getUserPage(current, size, keyword, status);
        return R.ok(pageResult);
    }

    /**
     * 分配角色
     */
    @PostMapping("/assign-roles")
    public R<Boolean> assignRoles(@Validated @RequestBody UserAssignRoleDTO dto) {
        Boolean result = userService.assignRoles(dto);
        return R.ok("角色分配成功", result);
    }

    /**
     * 分配角色（别名接口，兼容前端）
     */
    @PostMapping("/assign-role")
    public R<Boolean> assignRole(@Validated @RequestBody UserAssignRoleDTO dto) {
        return assignRoles(dto);
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public R<Boolean> changePassword(
            @RequestParam Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        Boolean result = userService.changePassword(userId, oldPassword, newPassword);
        return R.ok("密码修改成功", result);
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/reset-password")
    public R<Boolean> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        Boolean result = userService.resetPassword(id, newPassword);
        return R.ok("密码重置成功", result);
    }
}

