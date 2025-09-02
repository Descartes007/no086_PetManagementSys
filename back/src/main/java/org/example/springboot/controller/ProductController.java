package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Product;
import org.example.springboot.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 */
@Tag(name = "商品管理接口")
@RestController
@RequestMapping("/product")
public class ProductController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);
    
    @Resource
    private ProductService productService;
    
    @Operation(summary = "分页查询商品列表")
    @GetMapping("/page")
    public Result<?> getProductsByPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Product> page = productService.getProductsByPage(name, category, status,currentPage, size);
        return Result.success(page);
    }
    
    @Operation(summary = "根据ID获取商品")
    @GetMapping("/{id}")
    public Result<?> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return Result.success(product);
    }
    
    @Operation(summary = "创建商品")
    @PostMapping
    public Result<?> createProduct(@RequestBody Product product) {
        boolean success = productService.createProduct(product);
        if (success) {
            return Result.success("创建成功");
        } else {
            return Result.error("创建失败");
        }
    }
    
    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public Result<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        boolean success = productService.updateProduct(product);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }
    
    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        boolean success = productService.deleteProduct(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
    
    @Operation(summary = "获取热门商品")
    @GetMapping("/hot")
    public Result<?> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        List<Product> products = productService.getHotProducts(limit);
        return Result.success(products);
    }
    
    @Operation(summary = "获取推荐商品")
    @GetMapping("/recommend")
    public Result<?> getRecommendProducts(@RequestParam(defaultValue = "8") Integer limit) {
        List<Product> products = productService.getRecommendProducts(limit);
        return Result.success(products);
    }
} 