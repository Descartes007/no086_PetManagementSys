package org.example.springboot.DTO;

import lombok.Data;

import java.util.List;

/**
 * 统计数据传输对象
 */
@Data
public class StatisticsDTO {
    // 用户相关统计
    private Integer totalUserCount;
    private Integer maleUserCount;
    private Integer femaleUserCount;
    
    // 宠物相关统计
    private Integer totalPetCount;
    private Integer adoptedPetCount;
    private Integer availablePetCount;
    
    // 服务相关统计
    private Integer totalServiceAppointmentCount;
    private Integer completedServiceCount;
    private Integer pendingServiceCount;
    private Integer cancelledServiceCount;
    
    // 训练相关统计
    private Integer totalTrainingCount;
    private Integer completedTrainingCount;
    private Integer averageRating;
    
    // 寄养相关统计
    private Integer totalBoardingCount;
    private Integer activeBoardingCount;
    
    // 各类数据的月度趋势数据
    private List<MonthlyStatDTO> userTrend;
    private List<MonthlyStatDTO> petTrend;
    private List<MonthlyStatDTO> serviceTrend;
    private List<MonthlyStatDTO> trainingTrend;
    private List<MonthlyStatDTO> boardingTrend;
} 