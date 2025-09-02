package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Menu;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理接口")
@RestController
@RequestMapping("/menu")
public class MenuController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MenuController.class);

    @Resource
    private MenuService menuService;
    
    @Resource
    private UserMapper userMapper;

    @GetMapping("/role/{roleCode}")
    @Operation(summary = "获取指定角色的菜单")
    public Result<?> getMenusByRole(@PathVariable String roleCode) {
        List<Menu> menus = menuService.getMenusByRoleCode(roleCode);
        return Result.success(menus);
    }

    @PostMapping
    @Operation(summary = "保存菜单")
    public Result<?> save(@RequestBody Menu menu) {
        menuService.saveMenu(menu);
        return Result.success("保存成功");
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新菜单")
    public Result<?> update(@PathVariable Integer id, @RequestBody Menu menu) {
        menu.setId(id);
        menuService.updateMenu(menu);
        return Result.success("更新成功");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除菜单")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        menuService.deleteBatch(ids);
        return Result.success("删除成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    public Result<?> deleteById(@PathVariable Integer id) {
        menuService.deleteById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询菜单")
    public Result<?> findById(@PathVariable Integer id) {
        Menu menu = menuService.getById(id);
        return Result.success(menu);
    }

    @GetMapping
    @Operation(summary = "查询所有菜单(树形)")
    public Result<?> findAll() {
        List<Menu> menuTree = menuService.getAllMenuTree();
        return Result.success(menuTree);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询一级菜单")
    public Result<?> findParentMenus(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Menu> page = menuService.getParentMenuPage(currentPage, size);
        return Result.success(page);
    }

    @GetMapping("/children/{parentId}")
    @Operation(summary = "查询子菜单")
    public Result<?> findChildrenMenus(@PathVariable Integer parentId) {
        List<Menu> children = menuService.getChildrenMenus(parentId);
        return Result.success(children);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户的菜单树")
    public Result<?> getMenuTree(@PathVariable Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("-1", "用户不存在");
        }
        
        List<Menu> menus = menuService.getMenusByRoleCode(user.getRoleCode());
        return Result.success(menus);
    }

    @PostMapping("/role/{roleId}")
    @Operation(summary = "为角色分配菜单")
    public Result<?> assignMenusToRole(
            @PathVariable Integer roleId,
            @RequestBody List<Integer> menuIds) {
        menuService.assignMenusToRole(roleId, menuIds);
        return Result.success("分配成功");
    }
}