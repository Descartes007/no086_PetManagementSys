package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.AppointmentQueryDTO;
import org.example.springboot.DTO.ServiceAppointmentDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.ServiceAppointment;
import org.example.springboot.service.ServiceAppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * 服务预约管理接口
 */
@Tag(name = "服务预约管理接口")
@RestController
@RequestMapping("/service/appointment")
public class ServiceAppointmentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAppointmentController.class);

    @Resource
    private ServiceAppointmentService appointmentService;

    @Operation(summary = "创建预约")
    @PostMapping
    public Result<?> createAppointment(@RequestBody ServiceAppointment appointment) {
        LOGGER.info("创建预约: {}", appointment);
        Long id = appointmentService.createAppointment(appointment);
        return Result.success(id);
    }

    @Operation(summary = "根据ID获取预约详情")
    @GetMapping("/{id}")
    public Result<?> getAppointmentById(@PathVariable Long id) {
        LOGGER.info("获取预约详情: id={}", id);
        ServiceAppointmentDTO dto = appointmentService.getAppointmentById(id);
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
        
        AppointmentQueryDTO queryDTO = new AppointmentQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setStatus(status);
        queryDTO.setCurrentPage(currentPage);
        queryDTO.setSize(size);
        
        Page<ServiceAppointmentDTO> page = appointmentService.getAppointmentsByUserId(queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "分页查询所有预约列表（管理端）")
    @GetMapping("/page")
    public Result<?> getAppointmentsByPage(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String petName,
            @RequestParam(required = false) String contactPhone,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询预约列表: userId={}, serviceName={}, petName={}, contactPhone={}, status={}, " +
                "startDate={}, endDate={}, currentPage={}, size={}", 
                userId, serviceName, petName, contactPhone, status, startDate, endDate, currentPage, size);
        
        AppointmentQueryDTO queryDTO = new AppointmentQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setServiceName(serviceName);
        queryDTO.setPetName(petName);
        queryDTO.setContactPhone(contactPhone);
        queryDTO.setStatus(status);
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setCurrentPage(currentPage);
        queryDTO.setSize(size);
        
        Page<ServiceAppointmentDTO> page = appointmentService.getAppointmentsByPage(queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "取消预约（用户端）")
    @PutMapping("/{id}/cancel")
    public Result<?> cancelAppointment(@PathVariable Long id, @RequestBody ServiceAppointmentDTO dto) {
        LOGGER.info("取消预约: id={}, userId={}", id, dto.getUserId());
        if (dto.getUserId() == null) {
            return Result.error("用户ID不能为空");
        }
        
        boolean success = appointmentService.cancelAppointment(id, dto.getUserId());
        return success ? Result.success() : Result.error("取消预约失败");
    }

    @Operation(summary = "更新预约状态（管理端）")
    @PutMapping("/{id}/status")
    public Result<?> updateAppointmentStatus(@PathVariable Long id, @RequestBody ServiceAppointmentDTO dto) {
        LOGGER.info("更新预约状态: id={}, status={}, operatorId={}", id, dto.getStatus(), dto.getOperatorId());
        boolean success = appointmentService.updateAppointmentStatus(id, dto);
        return success ? Result.success() : Result.error("更新预约状态失败");
    }


} 