package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.PetCategory;
import org.example.springboot.service.PetCategoryService;
import org.example.springboot.service.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@Tag(name = "宠物分类接口")
@RestController
@RequestMapping("/pet-category")
public class PetCategoryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetCategoryController.class);
    
    @Resource
    private PetCategoryService petCategoryService;
    
    @Resource
    private PetService petService;
    
    @Operation(summary = "分页查询宠物分类")
    @GetMapping("/page")
    public Result<?> getCategoriesByPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询宠物分类");
        return Result.success(petCategoryService.getCategoriesByPage(name, parentId, status, currentPage, size));
    }
    
    @Operation(summary = "获取所有启用的宠物分类")
    @GetMapping("/all-enabled")
    public Result<?> getAllEnabledCategories() {
        LOGGER.info("获取所有启用的宠物分类");
        return Result.success(petCategoryService.getAllEnabledCategories());
    }

    @Operation(summary = "获取所有宠物分类")
    @GetMapping("/all")
    public Result<?> getAll() {
        LOGGER.info("获取所有宠物分类");
        return Result.success(petCategoryService.getCategoriesByPage(null, null, null, 1, Integer.MAX_VALUE).getRecords());
    }
    
    @Operation(summary = "获取分类树结构")
    @GetMapping("/tree")
    public Result<?> getCategoryTree(@RequestParam(required = false) Integer status) {
        LOGGER.info("获取分类树结构");
        return Result.success(petCategoryService.getCategoryTree(status));
    }
    
    @Operation(summary = "获取顶级分类")
    @GetMapping("/top")
    public Result<?> getTopCategories() {
        LOGGER.info("获取顶级分类");
        return Result.success(petCategoryService.getTopCategories());
    }
    
    @Operation(summary = "获取子分类")
    @GetMapping("/children/{parentId}")
    public Result<?> getChildCategories(@PathVariable Long parentId) {
        LOGGER.info("获取子分类，父ID: {}", parentId);
        return Result.success(petCategoryService.getChildCategories(parentId));
    }
    
    @Operation(summary = "根据ID获取分类")
    @GetMapping("/{id}")
    public Result<?> getCategoryById(@PathVariable Long id) {
        LOGGER.info("获取宠物分类详情，ID: {}", id);
        return Result.success(petCategoryService.getCategoryById(id));
    }
    
    @Operation(summary = "添加分类")
    @PostMapping
    public Result<?> addCategory(@RequestBody PetCategory category) {
        LOGGER.info("添加宠物分类: {}", category);
        petCategoryService.addCategory(category);
        return Result.success();
    }
    
    @Operation(summary = "更新分类")
    @PutMapping
    public Result<?> updateCategory(@RequestBody PetCategory category) {
        LOGGER.info("更新宠物分类: {}", category);
        petCategoryService.updateCategory(category);
        return Result.success();
    }
    
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        LOGGER.info("删除宠物分类，ID: {}", id);
        // 先检查该分类是否有宠物引用
        long count = petService.countByCategoryId(id);
        if (count > 0) {
            return Result.error("该分类下有" + count + "个宠物，无法删除");
        }
        petCategoryService.deleteCategory(id);
        return Result.success();
    }
    
    @Operation(summary = "更新分类状态")
    @PutMapping("/{id}/status/{status}")
    public Result<?> updateCategoryStatus(@PathVariable Long id, @PathVariable Integer status) {
        LOGGER.info("更新宠物分类状态，ID: {}, 状态: {}", id, status);
        petCategoryService.updateStatus(id, status);
        return Result.success();
    }
} 