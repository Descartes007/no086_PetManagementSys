package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Menu;
import org.example.springboot.entity.Role;
import org.example.springboot.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理接口")
@RestController
@RequestMapping("/role")
public class RoleController {
    
    @Resource
    private RoleService roleService;

    @Operation(summary = "创建角色")
    @PostMapping
    public Result<?> createRole(@RequestBody Role role) {
        roleService.createRole(role);
        return Result.success("创建成功");
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    public Result<?> updateRole(@PathVariable Integer id, @RequestBody Role role) {
        role.setId(id);
        roleService.updateRole(role);
        return Result.success("更新成功");
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<?> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取角色信息")
    @GetMapping("/{id}")
    public Result<?> getRoleById(@PathVariable Integer id) {
        Role role = roleService.getRoleById(id);
        return Result.success(role);
    }

    @Operation(summary = "获取所有角色")
    @GetMapping("/all")
    public Result<?> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    @Operation(summary = "分页查询角色")
    @GetMapping("/page")
    public Result<?> getRolesByPage(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Role> page = roleService.getRolesByPage(code, name, currentPage, pageSize);
        return Result.success(page);
    }

    @Operation(summary = "获取角色的菜单")
    @GetMapping("/{id}/menus")
    public Result<?> getRoleMenus(@PathVariable Integer id) {
        List<Menu> menus = roleService.getRoleMenus(id);
        return Result.success(menus);
    }

    @Operation(summary = "为角色分配菜单")
    @PostMapping("/{id}/menus")
    public Result<?> assignMenusToRole(
            @PathVariable("id") Integer roleId,
            @RequestBody List<Integer> menuIds) {
        roleService.assignMenusToRole(roleId, menuIds);
        return Result.success("菜单分配成功");
    }

    @Operation(summary = "获取角色的菜单ID列表")
    @GetMapping("/{id}/menuIds")
    public Result<?> getRoleMenuIds(@PathVariable("id") Integer roleId) {
        List<Integer> menuIds = roleService.getRoleMenuIds(roleId);
        return Result.success(menuIds);
    }
} 