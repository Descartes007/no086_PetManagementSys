package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.PieChartDTO;
import org.example.springboot.DTO.StatisticsDTO;
import org.example.springboot.common.Result;
import org.example.springboot.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据统计控制器
 */
@Tag(name = "数据统计接口")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    
    @Resource
    private StatisticsService statisticsService;
    
    @Operation(summary = "获取总体数据统计")
    @GetMapping("/overview")
    public Result<StatisticsDTO> getOverviewStatistics() {
        StatisticsDTO statistics = statisticsService.getOverviewStatistics();
        return Result.success(statistics);
    }
    
    @Operation(summary = "获取用户角色分布")
    @GetMapping("/user/role")
    public Result<List<PieChartDTO>> getUserRoleDistribution() {
        List<PieChartDTO> pieData = statisticsService.getUserRoleDistribution();
        return Result.success(pieData);
    }
    
    @Operation(summary = "获取宠物分类分布")
    @GetMapping("/pet/category")
    public Result<List<PieChartDTO>> getPetCategoryDistribution() {
        List<PieChartDTO> pieData = statisticsService.getPetCategoryDistribution();
        return Result.success(pieData);
    }
    
    @Operation(summary = "获取服务预约状态分布")
    @GetMapping("/service/status")
    public Result<List<PieChartDTO>> getServiceStatusDistribution() {
        List<PieChartDTO> pieData = statisticsService.getServiceStatusDistribution();
        return Result.success(pieData);
    }
    
    @Operation(summary = "获取训练预约状态分布")
    @GetMapping("/training/status")
    public Result<List<PieChartDTO>> getTrainingStatusDistribution() {
        List<PieChartDTO> pieData = statisticsService.getTrainingStatusDistribution();
        return Result.success(pieData);
    }
} 