package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;

import org.example.springboot.entity.Menu;
import org.example.springboot.entity.Role;
import org.example.springboot.entity.RoleMenu;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.MenuMapper;
import org.example.springboot.mapper.RoleMapper;
import org.example.springboot.mapper.RoleMenuMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private MenuMapper menuMapper;

    @Transactional
    public void createRole(Role role) {
        // 检查角色编码是否已存在
        if (isCodeExists(role.getCode())) {
            throw new ServiceException("角色编码已存在");
        }

        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        role.setCreatedTime(now);
        role.setUpdatedTime(now);

        if (roleMapper.insert(role) <= 0) {
            throw new ServiceException("角色创建失败");
        }
    }

    @Transactional
    public void updateRole(Role role) {
        // 检查角色是否存在
        Role existingRole = roleMapper.selectById(role.getId());
        if (existingRole == null) {
            throw new ServiceException("角色不存在");
        }

        // 如果修改了编码，检查新编码是否已存在
        if (!existingRole.getCode().equals(role.getCode()) && isCodeExists(role.getCode())) {
            throw new ServiceException("角色编码已存在");
        }

        // 设置更新时间
        role.setUpdatedTime(LocalDateTime.now());

        if (roleMapper.updateById(role) <= 0) {
            throw new ServiceException("角色更新失败");
        }
    }

    @Transactional
    public void deleteRole(Integer id) {
        // 检查角色是否存在
        if (roleMapper.selectById(id) == null) {
            throw new ServiceException("角色不存在");
        }

        // 检查是否有用户关联此角色
        // TODO: 根据实际情况添加用户角色关联的检查

        // 删除角色菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getRoleId, id));

        // 删除角色
        if (roleMapper.deleteById(id) <= 0) {
            throw new ServiceException("角色删除失败");
        }
    }

    public Role getRoleById(Integer id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }
        return role;
    }

    public Role getRoleByCode(String code) {
        return roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getCode, code));
    }

    public List<Role> getAllRoles() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<Role>()
                        .orderByAsc(Role::getCode));
    }

    public Page<Role> getRolesByPage(String code, String name, Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();

        if (code != null && !code.trim().isEmpty()) {
            queryWrapper.like(Role::getCode, code);
        }
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like(Role::getName, name);
        }

        queryWrapper.orderByAsc(Role::getCode);

        return roleMapper.selectPage(new Page<>(currentPage, pageSize), queryWrapper);
    }

    public List<Menu> getRoleMenus(Integer roleId) {
        // 获取角色菜单关联
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getRoleId, roleId));

        // 获取菜单ID列表
        List<Integer> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());

        // 如果没有关联菜单，返回空列表
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询菜单信息
        return menuMapper.selectList(
                new LambdaQueryWrapper<Menu>()
                        .in(Menu::getId, menuIds)
                        .orderByAsc(Menu::getSortNum));
    }

    @Transactional
    public void assignMenusToRole(Integer roleId, List<Integer> menuIds) {
        // 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }

        // 检查菜单是否都存在
        if (!menuIds.isEmpty()) {
            long count = menuMapper.selectCount(
                    new LambdaQueryWrapper<Menu>()
                            .in(Menu::getId, menuIds));
            if (count != menuIds.size()) {
                throw new ServiceException("存在无效的菜单ID");
            }
        }

        // 删除原有的角色菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getRoleId, roleId));

        // 批量插入新的角色菜单关联
        if (!menuIds.isEmpty()) {
            for (Integer menuId : menuIds) {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenu.setCreatedTime(LocalDateTime.now());
                roleMenuMapper.insert(roleMenu);
            }
        }

        // 更新角色的更新时间
        Role updateRole = new Role();
        updateRole.setId(roleId);
        updateRole.setUpdatedTime(LocalDateTime.now());
        roleMapper.updateById(updateRole);
    }

    public List<Integer> getRoleMenuIds(Integer roleId) {
        return roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getRoleId, roleId))
                .stream()
                .map(RoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    private boolean isCodeExists(String code) {
        return roleMapper.selectCount(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getCode, code)) > 0;
    }
}