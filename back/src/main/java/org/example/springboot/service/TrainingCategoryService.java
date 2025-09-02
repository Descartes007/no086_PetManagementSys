package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.entity.TrainingCategory;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.TrainingCategoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 训练分类服务类
 */
@Service
public class TrainingCategoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingCategoryService.class);

    @Resource
    private TrainingCategoryMapper categoryMapper;

    /**
     * 创建分类
     *
     * @param category 分类信息
     * @return 分类ID
     */
    public Long createCategory(TrainingCategory category) {
        if (category == null) {
            throw new ServiceException("分类信息不能为空");
        }
        
        // 校验分类信息
        if (StringUtils.isBlank(category.getName())) {
            throw new ServiceException("分类名称不能为空");
        }
        
        // 检查名称是否已存在
        LambdaQueryWrapper<TrainingCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TrainingCategory::getName, category.getName());
        if (categoryMapper.selectCount(queryWrapper) > 0) {
            throw new ServiceException("分类名称已存在");
        }
        
        // 设置默认值
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        
        // 设置创建时间
        category.setCreateTime(LocalDateTime.now());
        
        int result = categoryMapper.insert(category);
        if (result <= 0) {
            throw new ServiceException("创建分类失败");
        }
        
        return category.getId();
    }

    /**
     * 更新分类信息
     *
     * @param category 分类信息
     * @return 是否成功
     */
    public boolean updateCategory(TrainingCategory category) {
        if (category == null || category.getId() == null) {
            throw new ServiceException("分类信息不能为空");
        }
        
        // 检查分类是否存在
        TrainingCategory existingCategory = categoryMapper.selectById(category.getId());
        if (existingCategory == null) {
            throw new ServiceException("分类不存在");
        }
        
        // 检查名称是否已存在(排除自身)
        if (StringUtils.isNotBlank(category.getName()) && !category.getName().equals(existingCategory.getName())) {
            LambdaQueryWrapper<TrainingCategory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TrainingCategory::getName, category.getName())
                    .ne(TrainingCategory::getId, category.getId());
            if (categoryMapper.selectCount(queryWrapper) > 0) {
                throw new ServiceException("分类名称已存在");
            }
        }
        
        // 更新时间
        category.setUpdateTime(LocalDateTime.now());
        
        int result = categoryMapper.updateById(category);
        return result > 0;
    }

    /**
     * 获取分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    public TrainingCategory getCategoryById(Long id) {
        if (id == null) {
            throw new ServiceException("分类ID不能为空");
        }
        
        TrainingCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new ServiceException("分类不存在");
        }
        
        return category;
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    public boolean deleteCategory(Long id) {
        if (id == null) {
            throw new ServiceException("分类ID不能为空");
        }
        
        // 检查分类是否存在
        TrainingCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new ServiceException("分类不存在");
        }
        
        // 注意：实际应用中应该检查该分类是否已被课程引用
        // 这里简化处理，假设没有引用关系约束
        
        int result = categoryMapper.deleteById(id);
        return result > 0;
    }

    /**
     * 分页查询分类列表
     *
     * @param name     分类名称
     * @param status   状态
     * @param page     当前页
     * @param size     每页显示条数
     * @return 分页结果
     */
    public Page<TrainingCategory> getCategoriesByPage(String name, Integer status, Integer page, Integer size) {
        Page<TrainingCategory> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TrainingCategory> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(TrainingCategory::getName, name);
        }
        
        if (status != null) {
            queryWrapper.eq(TrainingCategory::getStatus, status);
        }
        
        // 按排序顺序和创建时间排序
        queryWrapper.orderByAsc(TrainingCategory::getSortOrder)
                .orderByDesc(TrainingCategory::getCreateTime);
        
        return categoryMapper.selectPage(pageParam, queryWrapper);
    }

    /**
     * 获取所有分类
     *
     * @return 分类列表
     */
    public List<TrainingCategory> getAllCategories() {
        LambdaQueryWrapper<TrainingCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(TrainingCategory::getSortOrder)
                .orderByDesc(TrainingCategory::getCreateTime);
        
        return categoryMapper.selectList(queryWrapper);
    }

    /**
     * 获取所有启用的分类
     *
     * @return 分类列表
     */
    public List<TrainingCategory> getAllEnabledCategories() {
        LambdaQueryWrapper<TrainingCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TrainingCategory::getStatus, 1)
                .orderByAsc(TrainingCategory::getSortOrder)
                .orderByDesc(TrainingCategory::getCreateTime);
        
        return categoryMapper.selectList(queryWrapper);
    }

    /**
     * 更新分类状态
     *
     * @param id     分类ID
     * @param status 状态(0:禁用,1:启用)
     * @return 是否成功
     */
    public boolean updateCategoryStatus(Long id, Integer status) {
        if (id == null) {
            throw new ServiceException("分类ID不能为空");
        }
        
        if (status == null || (status != 0 && status != 1)) {
            throw new ServiceException("状态值无效");
        }
        
        TrainingCategory category = new TrainingCategory();
        category.setId(id);
        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.now());
        
        int result = categoryMapper.updateById(category);
        return result > 0;
    }
} 