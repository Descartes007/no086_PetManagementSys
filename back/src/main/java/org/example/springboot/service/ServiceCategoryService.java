package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.entity.ServiceCategory;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ServiceCategoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.util.List;

/**
 * 服务分类业务类
 */
@Service
public class ServiceCategoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCategoryService.class);

    @Resource
    private ServiceCategoryMapper serviceCategoryMapper;

    /**
     * 获取所有分类列表
     *
     * @return 分类列表
     */
    public List<ServiceCategory> getAllCategories() {
        LambdaQueryWrapper<ServiceCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(ServiceCategory::getSort);
        return serviceCategoryMapper.selectList(queryWrapper);
    }

    /**
     * 获取所有启用状态的分类列表
     *
     * @return 启用状态的分类列表
     */
    public List<ServiceCategory> getEnabledCategories() {
        LambdaQueryWrapper<ServiceCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceCategory::getStatus, 1);
        queryWrapper.orderByAsc(ServiceCategory::getSort);
        return serviceCategoryMapper.selectList(queryWrapper);
    }

    /**
     * 根据ID获取分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    public ServiceCategory getCategoryById(Long id) {
        ServiceCategory category = serviceCategoryMapper.selectById(id);
        if (category == null) {
            throw new ServiceException("分类不存在");
        }
        return category;
    }

    /**
     * 添加分类
     *
     * @param category 分类信息
     * @return 添加结果
     */
    public boolean addCategory(ServiceCategory category) {
        if (category == null) {
            throw new ServiceException("分类信息不能为空");
        }
        
        // 设置初始状态为启用
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        
        validateCategory(category);
        
        // 检查是否有同名分类
        checkDuplicateCategoryName(category.getName(), null);
        
        int result = serviceCategoryMapper.insert(category);
        return result > 0;
    }

    /**
     * 更新分类
     *
     * @param category 分类信息
     * @return 更新结果
     */
    public boolean updateCategory(ServiceCategory category) {
        if (category == null || category.getId() == null) {
            throw new ServiceException("分类信息不完整");
        }
        
        // 检查分类是否存在
        ServiceCategory existingCategory = getCategoryById(category.getId());
        if (existingCategory == null) {
            throw new ServiceException("分类不存在");
        }
        
        validateCategory(category);
        
        // 检查是否有同名分类(排除自身)
        checkDuplicateCategoryName(category.getName(), category.getId());
        
        int result = serviceCategoryMapper.updateById(category);
        return result > 0;
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 删除结果
     */
    public boolean deleteCategory(Long id) {
        if (id == null) {
            throw new ServiceException("分类ID不能为空");
        }
        
        // 检查分类是否存在
        ServiceCategory existingCategory = getCategoryById(id);
        if (existingCategory == null) {
            throw new ServiceException("分类不存在");
        }
        
        // TODO: 检查分类下是否有服务，如有则不允许删除
        
        int result = serviceCategoryMapper.deleteById(id);
        return result > 0;
    }

    /**
     * 检查是否存在同名分类
     *
     * @param name 分类名称
     * @param id   分类ID（更新时排除自身）
     */
    private void checkDuplicateCategoryName(String name, Long id) {
        LambdaQueryWrapper<ServiceCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceCategory::getName, name);
        if (id != null) {
            queryWrapper.ne(ServiceCategory::getId, id);
        }
        
        Long count = serviceCategoryMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException("分类名称已存在");
        }
    }

    /**
     * 验证分类信息
     *
     * @param category 分类信息
     */
    private void validateCategory(ServiceCategory category) {
        if (StringUtils.isBlank(category.getName())) {
            throw new ServiceException("分类名称不能为空");
        }
        
        if (category.getSort() == null) {
            // 默认排序值为0
            category.setSort(0);
        }
    }
} 