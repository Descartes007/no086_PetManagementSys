package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.TrainingCourse;
import org.example.springboot.service.TrainingCourseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 训练课程管理接口
 */
@Tag(name = "训练课程管理接口")
@RestController
@RequestMapping("/training/course")
public class TrainingCourseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingCourseController.class);

    @Resource
    private TrainingCourseService courseService;

    @Operation(summary = "创建训练课程")
    @PostMapping
    public Result<?> createCourse(@RequestBody TrainingCourse course) {
        LOGGER.info("创建训练课程: {}", course);
        Long id = courseService.createCourse(course);
        return Result.success(id);
    }

    @Operation(summary = "更新训练课程")
    @PutMapping("/{id}")
    public Result<?> updateCourse(@PathVariable Long id, @RequestBody TrainingCourse course) {
        LOGGER.info("更新训练课程: id={}, course={}", id, course);
        course.setId(id);
        boolean success = courseService.updateCourse(course);
        return success ? Result.success() : Result.error("更新失败");
    }

    @Operation(summary = "获取训练课程详情")
    @GetMapping("/{id}")
    public Result<?> getCourseById(@PathVariable Long id) {
        LOGGER.info("获取训练课程详情: id={}", id);
        TrainingCourse course = courseService.getCourseById(id);
        return Result.success(course);
    }

    @Operation(summary = "删除训练课程")
    @DeleteMapping("/{id}")
    public Result<?> deleteCourse(@PathVariable Long id) {
        LOGGER.info("删除训练课程: id={}", id);
        boolean success = courseService.deleteCourse(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    @Operation(summary = "分页查询训练课程")
    @GetMapping("/page")
    public Result<?> getCoursesByPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询训练课程: name={}, category={}, categoryId={}, currentPage={}, size={}",
                name, category, categoryId, currentPage, size);
        
        Page<TrainingCourse> page = courseService.getCoursesByPage(name, category, status,categoryId, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "获取所有课程分类")
    @GetMapping("/categories")
    public Result<?> getAllCategories() {
        LOGGER.info("获取所有课程分类");
        List<String> categories = courseService.getAllCategories();
        return Result.success(categories);
    }

    @Operation(summary = "获取所有启用的课程")
    @GetMapping("/enabled")
    public Result<?> getAllEnabledCourses() {
        LOGGER.info("获取所有启用的课程");
        List<TrainingCourse> courses = courseService.getAllEnabledCourses();
        return Result.success(courses);
    }

    @Operation(summary = "获取指定分类的课程")
    @GetMapping("/category/{category}")
    public Result<?> getCoursesByCategory(@PathVariable String category) {
        LOGGER.info("获取指定分类的课程: category={}", category);
        List<TrainingCourse> courses = courseService.getCoursesByCategory(category);
        return Result.success(courses);
    }

    @Operation(summary = "更新课程状态")
    @PutMapping("/{id}/status")
    public Result<?> updateCourseStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        LOGGER.info("更新课程状态: id={}, status={}", id, status);
        boolean success = courseService.updateCourseStatus(id, status);
        return success ? Result.success() : Result.error("更新状态失败");
    }
} 