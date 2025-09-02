package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.entity.PetCategory;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.PetCategoryMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PetCategoryService {
    
    @Resource
    private PetCategoryMapper petCategoryMapper;
    
    /**
     * 分页查询宠物分类
     */
    public Page<PetCategory> getCategoriesByPage(String name, Long parentId, Integer status, 
                                               Integer currentPage, Integer size) {
        LambdaQueryWrapper<PetCategory> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(PetCategory::getName, name);
        }
        if (parentId != null) {
            queryWrapper.eq(PetCategory::getParentId, parentId);
        }
        if (status != null) {
            queryWrapper.eq(PetCategory::getStatus, status);
        }
        
        // 按排序字段和创建时间排序
        queryWrapper.orderByAsc(PetCategory::getSort);
        queryWrapper.orderByDesc(PetCategory::getCreateTime);
        
        return petCategoryMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }
    
    /**
     * 查询所有启用的宠物分类
     */
    public List<PetCategory> getAllEnabledCategories() {
        LambdaQueryWrapper<PetCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetCategory::getStatus, 1);
        queryWrapper.orderByAsc(PetCategory::getSort);
        return petCategoryMapper.selectList(queryWrapper);
    }
    
    /**
     * 查询顶级分类
     */
    public List<PetCategory> getTopCategories() {
        LambdaQueryWrapper<PetCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNull(PetCategory::getParentId).or().eq(PetCategory::getParentId, 0);
        queryWrapper.eq(PetCategory::getStatus, 1);
        queryWrapper.orderByAsc(PetCategory::getSort);
        return petCategoryMapper.selectList(queryWrapper);
    }
    
    /**
     * 查询子分类
     */
    public List<PetCategory> getChildCategories(Long parentId) {
        LambdaQueryWrapper<PetCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetCategory::getParentId, parentId);
        queryWrapper.eq(PetCategory::getStatus, 1);
        queryWrapper.orderByAsc(PetCategory::getSort);
        return petCategoryMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据ID获取分类
     */
    public PetCategory getCategoryById(Long id) {
        PetCategory category = petCategoryMapper.selectById(id);
        if (category == null) {
            throw new ServiceException("分类不存在");
        }
        return category;
    }
    
    /**
     * 添加分类
     */
    public void addCategory(PetCategory category) {
        // 如果没有设置状态，默认为启用
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        
        // 如果没有设置排序，默认为0
        if (category.getSort() == null) {
            category.setSort(0);
        }
        
        petCategoryMapper.insert(category);
    }
    
    /**
     * 更新分类
     */
    public void updateCategory(PetCategory category) {
        PetCategory dbCategory = getCategoryById(category.getId());
        petCategoryMapper.updateById(category);
    }
    
    /**
     * 删除分类
     */
    public void deleteCategory(Long id) {
        // 检查是否有子分类
        LambdaQueryWrapper<PetCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PetCategory::getParentId, id);
        long count = petCategoryMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new ServiceException("该分类下有子分类，无法删除");
        }
        
        // 检查该分类是否被宠物引用 (此处需要PetService的支持，暂不实现)
        // 如果有宠物引用该分类，应该抛出异常或者提供转移到其他分类的功能
        
        petCategoryMapper.deleteById(id);
    }
    
    /**
     * 启用/禁用分类
     */
    public void updateStatus(Long id, Integer status) {
        PetCategory category = getCategoryById(id);
        category.setStatus(status);
        petCategoryMapper.updateById(category);
    }
    
    /**
     * 构建分类树
     */
    public List<PetCategory> getCategoryTree(Integer status) {
        // 获取所有启用的分类
        LambdaQueryWrapper<PetCategory> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(PetCategory::getStatus, 1);
        if(status != null) {
            queryWrapper.eq(PetCategory::getStatus, status);
        }
        queryWrapper.orderByAsc(PetCategory::getSort);
        List<PetCategory> allCategories = petCategoryMapper.selectList(queryWrapper);
        
        // 按父ID分组
        Map<Long, List<PetCategory>> categoryMap = allCategories.stream()
                .collect(Collectors.groupingBy(category -> 
                    category.getParentId() == null ? 0L : category.getParentId()));
        
        // 构建树
        return buildCategoryTree(categoryMap, 0L);
    }
    
    /**
     * 递归构建分类树
     */
    private List<PetCategory> buildCategoryTree(Map<Long, List<PetCategory>> categoryMap, Long parentId) {
        List<PetCategory> result = new ArrayList<>();
        List<PetCategory> children = categoryMap.get(parentId);
        
        // 如果没有子节点，返回空列表
        if (children == null) {
            return result;
        }
        
        // 递归处理每个子节点
        for (PetCategory child : children) {
            child.setChildren(buildCategoryTree(categoryMap, child.getId()));
            result.add(child);
        }
        
        return result;
    }
} 