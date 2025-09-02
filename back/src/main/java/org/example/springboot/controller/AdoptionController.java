package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.AdoptionDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.service.AdoptionService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@Tag(name = "领养申请接口")
@RestController
@RequestMapping("/adoption")
public class AdoptionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdoptionController.class);

    @Resource
    private AdoptionService adoptionService;

    @Operation(summary = "分页查询领养申请")
    @GetMapping("/page")
    public Result<?> getAdoptionsByPage(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long petId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询领养申请");
        return Result.success(adoptionService.getAdoptionsByPage(userId, petId, status, currentPage, size));
    }

    @Operation(summary = "根据ID获取领养申请")
    @GetMapping("/{id}")
    public Result<?> getAdoptionById(@PathVariable Long id) {
        LOGGER.info("获取领养申请详情，ID: {}", id);
        return Result.success(adoptionService.getAdoptionById(id));
    }

    @Operation(summary = "创建领养申请")
    @PostMapping
    public Result<?> createAdoption(@RequestBody AdoptionDTO adoptionDTO) {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        Long userId = currentUser.getId();
        LOGGER.info("创建领养申请，用户ID: {}, 宠物ID: {}", userId, adoptionDTO.getPetId());
        adoptionService.createAdoption(userId, adoptionDTO);
        return Result.success();
    }

    @Operation(summary = "取消领养申请")
    @DeleteMapping("/{id}")
    public Result<?> cancelAdoption(@PathVariable Long id) {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        Long userId = currentUser.getId();
        LOGGER.info("取消领养申请，ID: {}, 用户ID: {}", id, userId);
        adoptionService.cancelAdoption(id, userId);
        return Result.success();
    }

    @Operation(summary = "审核领养申请")
    @PutMapping("/{id}/review")
    public Result<?> reviewAdoption(
            @PathVariable Long id,
            @RequestParam String status) {
        LOGGER.info("审核领养申请，ID: {}, 状态: {}", id, status);
        adoptionService.reviewAdoption(id, status);
        return Result.success();
    }

    @Operation(summary = "获取用户的申请记录")
    @GetMapping("/user")
    public Result<?> getUserAdoptions(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        Long userId = currentUser.getId();
        LOGGER.info("获取用户申请记录，用户ID: {}", userId);
        return Result.success(adoptionService.getUserAdoptions(userId, status, currentPage, size));
    }
} 