package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Product;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品服务实现类
 */
@Service
public class ProductService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    
    @Resource
    private ProductMapper productMapper;
    
    /**
     * 分页查询商品列表
     * @param name 商品名称
     * @param category 商品分类
     * @param currentPage 当前页
     * @param size 每页大小
     * @return 分页商品列表
     */
    public Page<Product> getProductsByPage(String name, String category, Integer status,Integer currentPage, Integer size) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(name)) {
            queryWrapper.like(Product::getName, name);
        }
        if (StringUtils.hasText(category)) {
            queryWrapper.eq(Product::getCategory, category);
        }
        
        // 默认只查询上架商品
        if(status != null) {
            queryWrapper.eq(Product::getStatus, status);
        }
//        queryWrapper.eq(Product::getStatus, 1);
        
        // 按ID降序排列
        queryWrapper.orderByDesc(Product::getId);
        
        return productMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }
    
    /**
     * 根据ID获取商品
     * @param id 商品ID
     * @return 商品对象
     */
    public Product getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        return product;
    }
    
    /**
     * 创建商品
     * @param product 商品对象
     * @return 是否成功
     */
    @Transactional
    public boolean createProduct(Product product) {
        // 设置默认值
        if (product.getStatus() == null) {
            product.setStatus(1); // 默认上架
        }
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        
        return productMapper.insert(product) > 0;
    }
    
    /**
     * 更新商品
     * @param product 商品对象
     * @return 是否成功
     */
    @Transactional
    public boolean updateProduct(Product product) {
        // 检查商品是否存在
        Product existingProduct = getProductById(product.getId());
        if (existingProduct == null) {
            throw new ServiceException("商品不存在");
        }
        
        product.setUpdateTime(LocalDateTime.now());
        return productMapper.updateById(product) > 0;
    }
    
    /**
     * 删除商品
     * @param id 商品ID
     * @return 是否成功
     */
    @Transactional
    public boolean deleteProduct(Long id) {
        // 检查商品是否存在
        Product existingProduct = getProductById(id);
        if (existingProduct == null) {
            throw new ServiceException("商品不存在");
        }
        
        return productMapper.deleteById(id) > 0;
    }
    
    /**
     * 获取热门商品
     * @param limit 限制数量
     * @return 热门商品列表
     */
    public List<Product> getHotProducts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认10个
        }
        return productMapper.getHotProducts(limit);
    }
    
    /**
     * 更新商品库存
     * @param id 商品ID
     * @param quantity 数量变化(减少为负)
     * @return 是否成功
     */
    @Transactional
    public boolean updateStock(Long id, Integer quantity) {
        if (quantity == 0) {
            return true; // 数量为0，无需更新
        }
        
        // 先检查库存是否充足
        Product product = getProductById(id);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        
        int newStock = product.getStock() + quantity;
        if (newStock < 0) {
            throw new ServiceException("商品库存不足");
        }
        
        // 更新库存
        LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Product::getId, id);
        updateWrapper.set(Product::getStock, newStock);
        updateWrapper.set(Product::getUpdateTime, LocalDateTime.now());
        
        return productMapper.update(null, updateWrapper) > 0;
    }
    
    /**
     * 获取推荐商品列表
     */
    public List<Product> getRecommendProducts(Integer limit) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        // 只查询上架商品
        queryWrapper.eq(Product::getStatus, 1);
        // 按创建时间倒序，获取最新商品
        queryWrapper.orderByDesc(Product::getCreateTime);
        // 限制返回数量
        queryWrapper.last("LIMIT " + limit);
        
        return productMapper.selectList(queryWrapper);
    }
} 