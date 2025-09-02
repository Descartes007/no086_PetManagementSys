package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ServiceCategory;
import org.example.springboot.service.ServiceCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务分类管理接口
 */
@Tag(name = "服务分类管理接口")
@RestController
@RequestMapping("/service/category")
public class ServiceCategoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCategoryController.class);

    @Resource
    private ServiceCategoryService serviceCategoryService;

    @Operation(summary = "获取所有分类列表")
    @GetMapping("/list")
    public Result<?> getAllCategories() {
        LOGGER.info("获取所有分类列表");
        List<ServiceCategory> list = serviceCategoryService.getAllCategories();
        return Result.success(list);
    }

    @Operation(summary = "获取所有启用状态的分类列表")
    @GetMapping("/enabled")
    public Result<?> getEnabledCategories() {
        LOGGER.info("获取所有启用状态的分类列表");
        List<ServiceCategory> list = serviceCategoryService.getEnabledCategories();
        return Result.success(list);
    }

    @Operation(summary = "根据ID获取分类详情")
    @GetMapping("/{id}")
    public Result<?> getCategoryById(@PathVariable Long id) {
        LOGGER.info("获取分类详情: id={}", id);
        ServiceCategory category = serviceCategoryService.getCategoryById(id);
        return Result.success(category);
    }

    @Operation(summary = "添加分类")
    @PostMapping
    public Result<?> addCategory(@RequestBody ServiceCategory category) {
        LOGGER.info("添加分类: {}", category);
        boolean success = serviceCategoryService.addCategory(category);
        return success ? Result.success(category.getId()) : Result.error("添加分类失败");
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody ServiceCategory category) {
        LOGGER.info("更新分类: id={}, category={}", id, category);
        category.setId(id);
        boolean success = serviceCategoryService.updateCategory(category);
        return success ? Result.success() : Result.error("更新分类失败");
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        LOGGER.info("删除分类: id={}", id);
        boolean success = serviceCategoryService.deleteCategory(id);
        return success ? Result.success() : Result.error("删除分类失败");
    }
} 