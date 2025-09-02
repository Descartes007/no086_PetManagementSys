package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.TrainingAppointmentDTO;
import org.example.springboot.DTO.TrainingQueryDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.TrainingAppointment;
import org.example.springboot.service.TrainingAppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 训练预约管理接口
 */
@Tag(name = "训练预约管理接口")
@RestController
@RequestMapping("/training/appointment")
public class TrainingAppointmentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingAppointmentController.class);

    @Resource
    private TrainingAppointmentService appointmentService;

    @Operation(summary = "创建训练预约")
    @PostMapping
    public Result<?> createAppointment(@RequestBody TrainingAppointment appointment) {
        LOGGER.info("创建训练预约: {}", appointment);
        Long id = appointmentService.createAppointment(appointment);
        return Result.success(id);
    }

    @Operation(summary = "根据ID获取预约详情")
    @GetMapping("/{id}")
    public Result<?> getAppointmentById(@PathVariable Long id) {
        LOGGER.info("获取预约详情: id={}", id);
        TrainingAppointmentDTO dto = appointmentService.getAppointmentById(id);
        return Result.success(dto);
    }

    @Operation(summary = "根据用户ID查询预约列表")
    @GetMapping("/user")
    public Result<?> getAppointmentsByUserId(
            @RequestParam Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("查询用户预约列表: userId={}, status={}, currentPage={}, size={}",
                userId, status, currentPage, size);
        
        TrainingQueryDTO queryDTO = new TrainingQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setStatus(status);
        queryDTO.setCurrentPage(currentPage);
        queryDTO.setSize(size);
        
        Page<TrainingAppointmentDTO> page = appointmentService.getAppointmentsByUserId(queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "分页查询所有预约列表（管理端）")
    @GetMapping("/page")
    public Result<?> getAppointmentsByPage(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String petName,
            @RequestParam(required = false) String contactPhone,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询预约列表: userId={}, courseId={}, courseName={}, petName={}, contactPhone={}, status={}, " +
                "startDate={}, endDate={}, currentPage={}, size={}",
                userId, courseId, courseName, petName, contactPhone, status, startDate, endDate, currentPage, size);
        
        TrainingQueryDTO queryDTO = new TrainingQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setCourseId(courseId);
        queryDTO.setCourseName(courseName);
        queryDTO.setPetName(petName);
        queryDTO.setContactPhone(contactPhone);
        queryDTO.setStatus(status);
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setCurrentPage(currentPage);
        queryDTO.setSize(size);
        
        Page<TrainingAppointmentDTO> page = appointmentService.getAppointmentsByPage(queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "取消预约（用户端）")
    @PutMapping("/{id}/cancel")
    public Result<?> cancelAppointment(@PathVariable Long id, @RequestBody TrainingAppointmentDTO dto) {
        LOGGER.info("取消预约: id={}, userId={}", id, dto.getUserId());
        if (dto.getUserId() == null) {
            return Result.error("用户ID不能为空");
        }
        
        boolean success = appointmentService.cancelAppointment(id, dto.getUserId());
        return success ? Result.success() : Result.error("取消预约失败");
    }

    @Operation(summary = "更新预约状态（管理端）")
    @PutMapping("/{id}/status")
    public Result<?> updateAppointmentStatus(@PathVariable Long id, @RequestBody TrainingAppointmentDTO dto) {
        LOGGER.info("更新预约状态: id={}, status={}, operatorId={}", id, dto.getStatus(), dto.getOperatorId());
        boolean success = appointmentService.updateAppointmentStatus(id, dto);
        return success ? Result.success() : Result.error("更新预约状态失败");
    }

    @Operation(summary = "更新训练进度")
    @PutMapping("/{id}/progress")
    public Result<?> updateProgress(
            @PathVariable Long id, 
            @RequestParam Integer progress,
            @RequestParam Long operatorId) {
        LOGGER.info("更新训练进度: id={}, progress={}, operatorId={}", id, progress, operatorId);
        boolean success = appointmentService.updateProgress(id, progress, operatorId);
        return success ? Result.success() : Result.error("更新训练进度失败");
    }

    @Operation(summary = "提交训练反馈")
    @PostMapping("/feedback")
    public Result<?> submitFeedback(@RequestBody TrainingAppointmentDTO dto) {
        LOGGER.info("提交训练反馈: appointmentId={}, userId={}, rating={}", 
                dto.getId(), dto.getUserId(), dto.getRating());
        
        if (dto.getId() == null) {
            return Result.error("预约ID不能为空");
        }
        
        if (dto.getUserId() == null) {
            return Result.error("用户ID不能为空");
        }
        
        if (dto.getRating() == null) {
            return Result.error("评分不能为空");
        }
        
        if (dto.getFeedback() == null || dto.getFeedback().isEmpty()) {
            return Result.error("反馈内容不能为空");
        }
        
        boolean success = appointmentService.submitFeedback(
                dto.getId(), dto.getUserId(), dto.getRating(), dto.getFeedback());
        
        return success ? Result.success() : Result.error("提交反馈失败");
    }
} 