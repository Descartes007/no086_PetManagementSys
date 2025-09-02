package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.MonthlyStatDTO;
import org.example.springboot.DTO.PieChartDTO;
import org.example.springboot.DTO.StatisticsDTO;
import org.example.springboot.entity.*;
import org.example.springboot.mapper.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据统计服务类
 */
@Service
public class StatisticsService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PetMapper petMapper;

    @Resource
    private ServiceAppointmentMapper serviceAppointmentMapper;

    @Resource
    private TrainingAppointmentMapper trainingAppointmentMapper;

    @Resource
    private BoardingMapper boardingMapper;

    /**
     * 获取概览统计数据
     */
    public StatisticsDTO getOverviewStatistics() {
        StatisticsDTO statistics = new StatisticsDTO();

        // 用户统计
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        statistics.setTotalUserCount(Math.toIntExact(userMapper.selectCount(userWrapper)));

        LambdaQueryWrapper<User> maleWrapper = new LambdaQueryWrapper<>();
        maleWrapper.eq(User::getSex, "男");
        statistics.setMaleUserCount(Math.toIntExact(userMapper.selectCount(maleWrapper)));

        LambdaQueryWrapper<User> femaleWrapper = new LambdaQueryWrapper<>();
        femaleWrapper.eq(User::getSex, "女");
        statistics.setFemaleUserCount(Math.toIntExact(userMapper.selectCount(femaleWrapper)));

        // 宠物统计
        LambdaQueryWrapper<Pet> petWrapper = new LambdaQueryWrapper<>();
        statistics.setTotalPetCount(Math.toIntExact(petMapper.selectCount(petWrapper)));

        LambdaQueryWrapper<Pet> adoptedWrapper = new LambdaQueryWrapper<>();
        adoptedWrapper.eq(Pet::getAdoptionStatus, "已领养");
        statistics.setAdoptedPetCount(Math.toIntExact(petMapper.selectCount(adoptedWrapper)));

        LambdaQueryWrapper<Pet> availableWrapper = new LambdaQueryWrapper<>();
        availableWrapper.eq(Pet::getAdoptionStatus, "待领养");
        statistics.setAvailablePetCount(Math.toIntExact(petMapper.selectCount(availableWrapper)));

        // 服务预约统计
        LambdaQueryWrapper<ServiceAppointment> serviceWrapper = new LambdaQueryWrapper<>();
        statistics.setTotalServiceAppointmentCount(Math.toIntExact(serviceAppointmentMapper.selectCount(serviceWrapper)));

        LambdaQueryWrapper<ServiceAppointment> completedServiceWrapper = new LambdaQueryWrapper<>();
        completedServiceWrapper.eq(ServiceAppointment::getStatus, "已完成");
        statistics.setCompletedServiceCount(Math.toIntExact(serviceAppointmentMapper.selectCount(completedServiceWrapper)));

        LambdaQueryWrapper<ServiceAppointment> pendingServiceWrapper = new LambdaQueryWrapper<>();
        pendingServiceWrapper.eq(ServiceAppointment::getStatus, "已预约");
        statistics.setPendingServiceCount(Math.toIntExact(serviceAppointmentMapper.selectCount(pendingServiceWrapper)));

        LambdaQueryWrapper<ServiceAppointment> cancelledServiceWrapper = new LambdaQueryWrapper<>();
        cancelledServiceWrapper.eq(ServiceAppointment::getStatus, "已取消");
        statistics.setCancelledServiceCount(Math.toIntExact(serviceAppointmentMapper.selectCount(cancelledServiceWrapper)));

        // 训练预约统计
        LambdaQueryWrapper<TrainingAppointment> trainingWrapper = new LambdaQueryWrapper<>();
        statistics.setTotalTrainingCount(Math.toIntExact(trainingAppointmentMapper.selectCount(trainingWrapper)));

        LambdaQueryWrapper<TrainingAppointment> completedTrainingWrapper = new LambdaQueryWrapper<>();
        completedTrainingWrapper.eq(TrainingAppointment::getStatus, "已完成");
        statistics.setCompletedTrainingCount(Math.toIntExact(trainingAppointmentMapper.selectCount(completedTrainingWrapper)));

        // 计算平均评分 (1-5分)
        LambdaQueryWrapper<TrainingAppointment> ratingWrapper = new LambdaQueryWrapper<>();
        ratingWrapper.isNotNull(TrainingAppointment::getRating);
        List<TrainingAppointment> ratedTrainings = trainingAppointmentMapper.selectList(ratingWrapper);
        if (!ratedTrainings.isEmpty()) {
            int sum = ratedTrainings.stream().mapToInt(TrainingAppointment::getRating).sum();
            statistics.setAverageRating(sum / ratedTrainings.size());
        } else {
            statistics.setAverageRating(0);
        }

        // 寄养统计
        LambdaQueryWrapper<Boarding> boardingWrapper = new LambdaQueryWrapper<>();
        statistics.setTotalBoardingCount(Math.toIntExact(boardingMapper.selectCount(boardingWrapper)));

        LambdaQueryWrapper<Boarding> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Boarding::getStatus, "进行中");
        statistics.setActiveBoardingCount(Math.toIntExact(boardingMapper.selectCount(activeWrapper)));

        // 设置月度趋势数据
        statistics.setUserTrend(getUserMonthlyTrend());
        statistics.setPetTrend(getPetMonthlyTrend());
        statistics.setServiceTrend(getServiceMonthlyTrend());
        statistics.setTrainingTrend(getTrainingMonthlyTrend());
        statistics.setBoardingTrend(getBoardingMonthlyTrend());

        return statistics;
    }

    /**
     * 获取用户角色分布
     */
    public List<PieChartDTO> getUserRoleDistribution() {
        List<User> users = userMapper.selectList(null);
        Map<String, Long> roleCounts = users.stream()
                .collect(Collectors.groupingBy(User::getRoleCode, Collectors.counting()));

        List<PieChartDTO> pieData = new ArrayList<>();
        roleCounts.forEach((role, count) -> {
            PieChartDTO data = new PieChartDTO();
            String roleName = "未知角色";
            if ("ADMIN".equals(role)) {
                roleName = "管理员";
            } else if ("USER".equals(role)) {
                roleName = "普通用户";
            }
            data.setName(roleName);
            data.setValue(count.intValue());
            pieData.add(data);
        });

        return pieData;
    }

    /**
     * 获取宠物分类分布
     */
    public List<PieChartDTO> getPetCategoryDistribution() {
        List<Pet> pets = petMapper.selectList(null);
        Map<Long, Long> categoryCounts = pets.stream()
                .collect(Collectors.groupingBy(Pet::getCategoryId, Collectors.counting()));

        List<PieChartDTO> pieData = new ArrayList<>();
        categoryCounts.forEach((categoryId, count) -> {
            PieChartDTO data = new PieChartDTO();
            
            // 这里需要从数据库获取实际分类名
            // 简化处理，实际应该关联查询分类表
            String categoryName = "分类" + categoryId;
            
            data.setName(categoryName);
            data.setValue(count.intValue());
            pieData.add(data);
        });

        return pieData;
    }

    /**
     * 获取服务预约状态分布
     */
    public List<PieChartDTO> getServiceStatusDistribution() {
        List<ServiceAppointment> appointments = serviceAppointmentMapper.selectList(null);
        Map<String, Long> statusCounts = appointments.stream()
                .collect(Collectors.groupingBy(ServiceAppointment::getStatus, Collectors.counting()));

        List<PieChartDTO> pieData = new ArrayList<>();
        statusCounts.forEach((status, count) -> {
            PieChartDTO data = new PieChartDTO();
            data.setName(status);
            data.setValue(count.intValue());
            pieData.add(data);
        });

        return pieData;
    }

    /**
     * 获取训练预约状态分布
     */
    public List<PieChartDTO> getTrainingStatusDistribution() {
        List<TrainingAppointment> appointments = trainingAppointmentMapper.selectList(null);
        Map<String, Long> statusCounts = appointments.stream()
                .collect(Collectors.groupingBy(TrainingAppointment::getStatus, Collectors.counting()));

        List<PieChartDTO> pieData = new ArrayList<>();
        statusCounts.forEach((status, count) -> {
            PieChartDTO data = new PieChartDTO();
            data.setName(status);
            data.setValue(count.intValue());
            pieData.add(data);
        });

        return pieData;
    }

    /**
     * 获取近6个月用户增长趋势
     */
    private List<MonthlyStatDTO> getUserMonthlyTrend() {
        return getMonthlyTrend(userMapper.selectList(null), User::getCreateTime);
    }

    /**
     * 获取近6个月宠物增长趋势
     */
    private List<MonthlyStatDTO> getPetMonthlyTrend() {
        return getMonthlyTrend(petMapper.selectList(null), Pet::getCreateTime);
    }

    /**
     * 获取近6个月服务预约趋势
     */
    private List<MonthlyStatDTO> getServiceMonthlyTrend() {
        return getMonthlyTrend(serviceAppointmentMapper.selectList(null), ServiceAppointment::getCreateTime);
    }

    /**
     * 获取近6个月训练预约趋势
     */
    private List<MonthlyStatDTO> getTrainingMonthlyTrend() {
        return getMonthlyTrend(trainingAppointmentMapper.selectList(null), TrainingAppointment::getCreateTime);
    }

    /**
     * 获取近6个月寄养申请趋势
     */
    private List<MonthlyStatDTO> getBoardingMonthlyTrend() {
        return getMonthlyTrend(boardingMapper.selectList(null), Boarding::getCreateTime);
    }

    /**
     * 获取实体的月度趋势数据
     */
    private <T> List<MonthlyStatDTO> getMonthlyTrend(List<T> entities, java.util.function.Function<T, LocalDateTime> timeExtractor) {
        // 获取最近6个月的时间
        List<String> last6Months = getLast6Months();
        
        // 按月份分组统计
        Map<String, Long> monthlyCounts = entities.stream()
                .filter(entity -> timeExtractor.apply(entity) != null)
                .collect(Collectors.groupingBy(
                        entity -> timeExtractor.apply(entity).format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.counting()));
        
        // 构建结果
        List<MonthlyStatDTO> result = new ArrayList<>();
        for (String month : last6Months) {
            MonthlyStatDTO stat = new MonthlyStatDTO();
            stat.setMonth(month);
            stat.setCount(monthlyCounts.getOrDefault(month, 0L).intValue());
            result.add(stat);
        }
        
        return result;
    }

    /**
     * 获取最近6个月的年月
     */
    private List<String> getLast6Months() {
        List<String> months = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (int i = 5; i >= 0; i--) {
            LocalDateTime targetMonth = now.minusMonths(i);
            months.add(targetMonth.format(formatter));
        }
        
        return months;
    }
} 