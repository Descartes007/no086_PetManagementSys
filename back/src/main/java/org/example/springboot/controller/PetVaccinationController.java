package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.PetVaccinationDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.PetVaccination;
import org.example.springboot.service.PetVaccinationService;
import org.springframework.web.bind.annotation.*;



/**
 * 宠物疫苗接种记录控制器
 */
@Tag(name = "宠物疫苗接种记录管理接口")
@RestController
@RequestMapping("/pet-vaccination")
public class PetVaccinationController {
    
    @Resource
    private PetVaccinationService petVaccinationService;
    
    @Operation(summary = "添加疫苗接种记录")
    @PostMapping
    public Result<?> add(@RequestBody PetVaccination vaccination) {
        return Result.success(petVaccinationService.addVaccination(vaccination));
    }
    
    @Operation(summary = "更新疫苗接种记录")
    @PutMapping
    public Result<?> update(@RequestBody PetVaccination vaccination) {
        return Result.success(petVaccinationService.updateVaccination(vaccination));
    }
    
    @Operation(summary = "删除疫苗接种记录")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        petVaccinationService.deleteVaccination(id);
        return Result.success();
    }
    
    @Operation(summary = "根据ID获取疫苗接种记录")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(petVaccinationService.getVaccinationById(id));
    }
    
    @Operation(summary = "分页查询疫苗接种记录")
    @GetMapping("/page")
    public Result<?> page(
            @RequestParam(required = false) Long petId,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<PetVaccinationDTO> page = petVaccinationService.getVaccinations(petId, currentPage, size);
        return Result.success(page);
    }
} 