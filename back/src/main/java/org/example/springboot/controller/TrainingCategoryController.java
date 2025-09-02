package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.TrainingCategory;
import org.example.springboot.service.TrainingCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 训练分类管理接口
 */
@Tag(name = "训练分类管理接口")
@RestController
@RequestMapping("/training/category")
public class TrainingCategoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingCategoryController.class);

    @Resource
    private TrainingCategoryService categoryService;

    @Operation(summary = "创建训练分类")
    @PostMapping
    public Result<?> createCategory(@RequestBody TrainingCategory category) {
        LOGGER.info("创建训练分类: {}", category);
        Long id = categoryService.createCategory(category);
        return Result.success(id);
    }

    @Operation(summary = "更新训练分类")
    @PutMapping("/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody TrainingCategory category) {
        LOGGER.info("更新训练分类: id={}, category={}", id, category);
        category.setId(id);
        boolean success = categoryService.updateCategory(category);
        return success ? Result.success() : Result.error("更新失败");
    }

    @Operation(summary = "获取训练分类详情")
    @GetMapping("/{id}")
    public Result<?> getCategoryById(@PathVariable Long id) {
        LOGGER.info("获取训练分类详情: id={}", id);
        TrainingCategory category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    @Operation(summary = "删除训练分类")
    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        LOGGER.info("删除训练分类: id={}", id);
        boolean success = categoryService.deleteCategory(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    @Operation(summary = "分页查询训练分类")
    @GetMapping("/page")
    public Result<?> getCategoriesByPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询训练分类: name={}, status={}, currentPage={}, size={}",
                name, status, currentPage, size);
        
        Page<TrainingCategory> page = categoryService.getCategoriesByPage(name, status, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "获取所有分类")
    @GetMapping("/list")
    public Result<?> getAllCategories() {
        LOGGER.info("获取所有训练分类");
        List<TrainingCategory> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @Operation(summary = "获取所有启用的分类")
    @GetMapping("/enabled")
    public Result<?> getAllEnabledCategories() {
        LOGGER.info("获取所有启用的训练分类");
        List<TrainingCategory> categories = categoryService.getAllEnabledCategories();
        return Result.success(categories);
    }

    @Operation(summary = "更新分类状态")
    @PutMapping("/{id}/status")
    public Result<?> updateCategoryStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        LOGGER.info("更新分类状态: id={}, status={}", id, status);
        boolean success = categoryService.updateCategoryStatus(id, status);
        return success ? Result.success() : Result.error("更新状态失败");
    }
} 