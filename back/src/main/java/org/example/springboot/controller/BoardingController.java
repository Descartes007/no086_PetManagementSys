package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Boarding;
import org.example.springboot.service.BoardingService;
import org.springframework.web.bind.annotation.*;



/**
 * 寄养信息控制器
 */
@Tag(name = "寄养管理接口")
@RestController
@RequestMapping("/boarding")
public class BoardingController {
    @Resource
    private BoardingService boardingService;

    @Operation(summary = "分页查询寄养信息")
    @GetMapping("/page")
    public Result<?> getBoardingsByPage(
            @RequestParam(defaultValue = "") String petName,
            @RequestParam(defaultValue = "") String petType,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Boarding> page = boardingService.getBoardingsByPage(petName, petType, status, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "根据用户ID分页查询寄养信息")
    @GetMapping("/my")
    public Result<?> getBoardingsByUserId(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Boarding> page = boardingService.getBoardingsByUserId(userId, status, currentPage, size);
        return Result.success(page);
    }

    @Operation(summary = "根据ID获取寄养信息")
    @GetMapping("/{id}")
    public Result<?> getBoardingById(@PathVariable Long id) {
        Boarding boarding = boardingService.getBoardingById(id);
        return Result.success(boarding);
    }

    @Operation(summary = "添加寄养信息")
    @PostMapping
    public Result<?> addBoarding(@RequestBody Boarding boarding) {
        Boarding result = boardingService.addBoarding(boarding);
        return Result.success(result);
    }

    @Operation(summary = "更新寄养信息")
    @PutMapping
    public Result<?> updateBoarding(@RequestBody Boarding boarding) {
        Boarding result = boardingService.updateBoarding(boarding);
        return Result.success(result);
    }

    @Operation(summary = "更新寄养状态")
    @PutMapping("/{id}/status")
    public Result<?> updateBoardingStatus(@PathVariable Long id, @RequestParam String status) {
        Boarding result = boardingService.updateBoardingStatus(id, status);
        return Result.success(result);
    }

    @Operation(summary = "取消寄养申请")
    @PutMapping("/{id}/cancel")
    public Result<?> cancelBoarding(@PathVariable Long id) {
        Boarding result = boardingService.cancelBoarding(id);
        return Result.success(result);
    }

    @Operation(summary = "删除寄养信息")
    @DeleteMapping("/{id}")
    public Result<?> deleteBoarding(@PathVariable Long id) {
        boardingService.deleteBoarding(id);
        return Result.success();
    }
} 