package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.springboot.DTO.PetHealthRecordDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.PetHealthRecord;
import org.example.springboot.service.PetHealthRecordService;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

/**
 * 宠物健康记录控制器
 */
@Tag(name = "宠物健康记录管理接口")
@RestController
@RequestMapping("/pet-health-record")
public class PetHealthRecordController {
    
    @Resource
    private PetHealthRecordService petHealthRecordService;
    
    @Operation(summary = "添加健康记录")
    @PostMapping
    public Result<?> add(@RequestBody PetHealthRecord record) {
        return Result.success(petHealthRecordService.addHealthRecord(record));
    }
    
    @Operation(summary = "更新健康记录")
    @PutMapping
    public Result<?> update(@RequestBody PetHealthRecord record) {
        return Result.success(petHealthRecordService.updateHealthRecord(record));
    }
    
    @Operation(summary = "删除健康记录")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        petHealthRecordService.deleteHealthRecord(id);
        return Result.success();
    }
    
    @Operation(summary = "根据ID获取健康记录")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(petHealthRecordService.getHealthRecordById(id));
    }
    
    @Operation(summary = "分页查询健康记录")
    @GetMapping("/page")
    public Result<?> page(
            @RequestParam(required = false) Long petId,
            @RequestParam(required = false) String recordType,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<PetHealthRecordDTO> page = petHealthRecordService.getHealthRecords(petId, recordType, currentPage, size);
        return Result.success(page);
    }
} 