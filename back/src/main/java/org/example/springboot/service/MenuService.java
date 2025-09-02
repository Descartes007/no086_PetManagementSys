package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.controller.UserController;
import org.example.springboot.entity.Menu;

import org.example.springboot.entity.Role;
import org.example.springboot.entity.RoleMenu;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.MenuMapper;
import org.example.springboot.mapper.RoleMapper;
import org.example.springboot.mapper.RoleMenuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MenuService.class);
    @Resource
    private MenuMapper menuMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private RoleMapper roleMapper;

    /**
     * 为角色分配菜单
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignMenusToRole(Integer roleId, List<Integer> menuIds) {
        // 1. 检查角色是否存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ServiceException("角色不存在");
        }

        // 2. 删除原有的角色菜单关联
        LambdaQueryWrapper<RoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleMenu::getRoleId, roleId);
        roleMenuMapper.delete(wrapper);

        // 3. 批量插入新的角色菜单关联
        for (Integer menuId : menuIds) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenu.setCreatedTime(LocalDateTime.now());
            roleMenuMapper.insert(roleMenu);
        }
    }

    /**
     * 保存或更新菜单
     */
    public void saveMenu(Menu menu) {

        if (menu.getId() != null) {

            if (menuMapper.updateById(menu) <= 0) {
                throw new ServiceException("更新失败");
            }
        } else {

            if (menuMapper.insert(menu) <= 0) {
                throw new ServiceException("新增失败");
            }
        }
    }

    /**
     * 更新菜单
     */
    public void updateMenu(Menu menu) {
        Menu existMenu = menuMapper.selectById(menu.getId());
        if (existMenu == null) {
            throw new ServiceException("菜单不存在");
        }
        menu.setUpdatedTime(LocalDateTime.now());
        if (menuMapper.updateById(menu) <= 0) {
            throw new ServiceException("更新失败");
        }
    }

    /**
     * 删除菜单
     */
    public void deleteById(Integer id) {
        // 检查是否存在子菜单
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getPid, id);
        Long count = menuMapper.selectCount(wrapper);
        if (count > 0) {
            throw new ServiceException("存在子菜单,不能删除");
        }
        
        if (menuMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除失败");
        }
    }

    /**
     * 批量删除菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Integer> ids) {
        // 检查是否存在子菜单
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Menu::getPid, ids);
        Long count = menuMapper.selectCount(wrapper);
        if (count > 0) {
            throw new ServiceException("选中的菜单中存在子菜单,不能删除");
        }

        if (menuMapper.deleteByIds(ids) <= 0) {
            throw new ServiceException("删除失败");
        }
    }

    /**
     * 根据ID查询菜单
     */
    public Menu getById(Integer id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new ServiceException("菜单不存在");
        }
        return menu;
    }

    /**
     * 获取所有菜单树
     */
    public List<Menu> getAllMenuTree() {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Menu::getSortNum);
        List<Menu> allMenus = menuMapper.selectList(wrapper);
        return buildMenuTree(allMenus);
    }

    /**
     * 分页查询父级菜单
     */
    public Page<Menu> getParentMenuPage(Integer currentPage, Integer size) {
        Page<Menu> page = new Page<>(currentPage, size);
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Menu::getPid)
              .orderByAsc(Menu::getSortNum);
        return menuMapper.selectPage(page, wrapper);
    }

    /**
     * 查询子菜单
     */
    public List<Menu> getChildrenMenus(Integer parentId) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getPid, parentId)
              .orderByAsc(Menu::getSortNum);
        return menuMapper.selectList(wrapper);
    }

    /**
     * 根据角色编码获取菜单
     */
    public List<Menu> getMenusByRoleCode(String roleCode) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            LOGGER.warn("Role code is null or empty");
            return new ArrayList<>();
        }

        try {
            // 1. 获取角色
            LambdaQueryWrapper<Role> roleWrapper = new LambdaQueryWrapper<>();
            roleWrapper.eq(Role::getCode, roleCode);
            Role role = roleMapper.selectOne(roleWrapper);
            if (role == null) {
                LOGGER.warn("Role not found for code: {}", roleCode);
                return new ArrayList<>();
            }

            // 2. 获取角色菜单关联
            LambdaQueryWrapper<RoleMenu> rmWrapper = new LambdaQueryWrapper<>();
            rmWrapper.eq(RoleMenu::getRoleId, role.getId());
            List<RoleMenu> roleMenus = roleMenuMapper.selectList(rmWrapper);
            if (roleMenus.isEmpty()) {
                LOGGER.info("No menus found for role: {}", roleCode);
                return new ArrayList<>();
            }

            // 3. 获取菜单ID列表
            List<Integer> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

            if (menuIds.isEmpty()) {
                LOGGER.warn("No valid menu IDs found for role: {}", roleCode);
                return new ArrayList<>();
            }

            // 4. 使用IN条件批量查询菜单
            LambdaQueryWrapper<Menu> menuWrapper = new LambdaQueryWrapper<>();
            menuWrapper.in(Menu::getId, menuIds)
                      .orderByAsc(Menu::getSortNum);
            List<Menu> menus = menuMapper.selectList(menuWrapper);

            if (menus.isEmpty()) {
                LOGGER.warn("No menus found for menu IDs: {}", menuIds);
                return new ArrayList<>();
            }

            // 5. 构建树形结构
            return buildMenuTree(menus);

        } catch (Exception e) {
            LOGGER.error("Error in getMenusByRoleCode for role {}: {}", roleCode, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建菜单树
     */
    private List<Menu> buildMenuTree(List<Menu> menus) {
        List<Menu> trees = new ArrayList<>();
        for (Menu menu : menus) {
            if (menu.getPid() == null) {
                menu.setChildren(getChildren(menu, menus));
                trees.add(menu);
            }
        }
        return trees;
    }

    /**
     * 递归获取子菜单
     */
    private List<Menu> getChildren(Menu parent, List<Menu> allMenus) {
        List<Menu> children = new ArrayList<>();
        for (Menu menu : allMenus) {
            if (parent.getId().equals(menu.getPid())) {
                menu.setChildren(getChildren(menu, allMenus));
                children.add(menu);
            }
        }
        return children;
    }
} 