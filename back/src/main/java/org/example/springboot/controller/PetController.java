package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.PetWithAdoptionStatusDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Pet;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.service.PetService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "宠物管理接口")
@RestController
@RequestMapping("/pet")
public class PetController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetController.class);

    @Resource
    private PetService petService;

    @Operation(summary = "分页查询宠物")
    @GetMapping("/page")
    public Result<?> getPetsByPage(
            @RequestParam(required = false) String name,

            @RequestParam(required = false) String breed,
            @RequestParam(required = false) String adoptionStatus,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询宠物列表");
        return Result.success(petService.getPetsByPage(name, breed, adoptionStatus, categoryId, currentPage, size));
    }

    @Operation(summary = "根据ID获取宠物")
    @GetMapping("/{id}")
    public Result<?> getPetById(@PathVariable Long id) {
        LOGGER.info("获取宠物详情，ID: {}", id);
        return Result.success(petService.getPetById(id));
    }

    @Operation(summary = "添加宠物")
    @PostMapping
    public Result<?> addPet(@RequestBody Pet pet) {
        LOGGER.info("添加宠物: {}", pet);
        petService.addPet(pet);
        return Result.success();
    }

    @Operation(summary = "更新宠物信息")
    @PutMapping
    public Result<?> updatePet(@RequestBody Pet pet) {
        LOGGER.info("更新宠物信息: {}", pet);
        petService.updatePet(pet);
        return Result.success();
    }

    @Operation(summary = "删除宠物")
    @DeleteMapping("/{id}")
    public Result<?> deletePet(@PathVariable Long id) {
        LOGGER.info("删除宠物，ID: {}", id);
        petService.deletePet(id);
        return Result.success();
    }
    
    @Operation(summary = "获取宠物列表带用户申请状态")
    @GetMapping("/with-user-status")
    public Result<?> getPetsWithUserStatus() {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        Long userId = currentUser.getId();
        LOGGER.info("获取带用户申请状态的宠物列表，用户ID: {}", userId);
        List<PetWithAdoptionStatusDTO> result = petService.getPetsWithUserAdoptionStatus(userId);
        return Result.success(result);
    }
    
    @Operation(summary = "获取宠物详情带用户申请状态")
    @GetMapping("/{id}/with-user-status")
    public Result<?> getPetWithUserStatus(@PathVariable Long id) {
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录");
        }
        Long userId = currentUser.getId();
        LOGGER.info("获取带用户申请状态的宠物详情，宠物ID: {}, 用户ID: {}", id, userId);
        PetWithAdoptionStatusDTO result = petService.getPetWithUserAdoptionStatus(id, userId);
        return Result.success(result);
    }

    @Operation(summary = "获取推荐宠物")
    @GetMapping("/recommend")
    public Result<?> getRecommendPets(@RequestParam(defaultValue = "8") Integer limit) {
        LOGGER.info("获取推荐宠物列表，数量限制：{}", limit);
        return Result.success(petService.getRecommendPets(limit));
    }
} 