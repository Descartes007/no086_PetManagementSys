package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.DTO.AppointmentQueryDTO;
import org.example.springboot.DTO.ServiceAppointmentDTO;
import org.example.springboot.entity.Service;
import org.example.springboot.entity.ServiceAppointment;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ServiceAppointmentMapper;
import org.example.springboot.mapper.ServiceMapper;
import org.example.springboot.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 服务预约业务类
 */
@org.springframework.stereotype.Service
public class ServiceAppointmentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAppointmentService.class);

    @Resource
    private ServiceAppointmentMapper appointmentMapper;

    @Resource
    private ServiceMapper serviceMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 创建预约
     *
     * @param appointment 预约信息
     * @return 预约ID
     */
    public Long createAppointment(ServiceAppointment appointment) {
        if (appointment == null) {
            throw new ServiceException("预约信息不能为空");
        }

        validateAppointment(appointment);

        // 设置初始状态为已预约
        appointment.setStatus("已预约");

        int result = appointmentMapper.insert(appointment);
        if (result <= 0) {
            throw new ServiceException("创建预约失败");
        }

        return appointment.getId();
    }

    /**
     * 验证预约信息
     */
    private void validateAppointment(ServiceAppointment appointment) {
        if (appointment.getUserId() == null) {
            throw new ServiceException("用户ID不能为空");
        }

        if (appointment.getServiceId() == null) {
            throw new ServiceException("服务ID不能为空");
        }

        if (appointment.getAppointmentTime() == null) {
            throw new ServiceException("预约时间不能为空");
        }

        // 预约时间不能早于当前时间
        if (appointment.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException("预约时间不能早于当前时间");
        }

        if (StringUtils.isBlank(appointment.getPetName())) {
            throw new ServiceException("宠物名称不能为空");
        }

        if (StringUtils.isBlank(appointment.getContactPhone())) {
            throw new ServiceException("联系电话不能为空");
        }

        // 验证服务是否存在且状态正常
        Service service = serviceMapper.selectById(appointment.getServiceId());
        if (service == null) {
            throw new ServiceException("所选服务不存在");
        }

        if (service.getStatus() != 1) {
            throw new ServiceException("所选服务暂不可预约");
        }

        // 验证用户是否存在
        User user = userMapper.selectById(appointment.getUserId());
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
    }

    /**
     * 根据ID获取预约详情
     *
     * @param id 预约ID
     * @return 预约详情
     */
    public ServiceAppointmentDTO getAppointmentById(Long id) {
        if (id == null) {
            throw new ServiceException("预约ID不能为空");
        }

        ServiceAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new ServiceException("预约不存在");
        }

        return convertToDTO(appointment);
    }

    /**
     * 将实体转换为DTO
     */
    private ServiceAppointmentDTO convertToDTO(ServiceAppointment appointment) {
        ServiceAppointmentDTO dto = new ServiceAppointmentDTO();
        dto.setId(appointment.getId());
        dto.setUserId(appointment.getUserId());
        dto.setServiceId(appointment.getServiceId());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setPetName(appointment.getPetName());
        dto.setRequirements(appointment.getRequirements());
        dto.setContactPhone(appointment.getContactPhone());
        dto.setCreateTime(appointment.getCreateTime());
        dto.setUpdateTime(appointment.getUpdateTime());

        // 设置服务名称
        Service service = serviceMapper.selectById(appointment.getServiceId());
        if (service != null) {
            dto.setServiceName(service.getName());
        }

        // 设置用户名称
        User user = userMapper.selectById(appointment.getUserId());
        if (user != null) {
            dto.setUserName(user.getName() != null ? user.getName() : user.getUsername());
        }

        return dto;
    }

    /**
     * 根据用户ID分页查询预约列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    public Page<ServiceAppointmentDTO> getAppointmentsByUserId(AppointmentQueryDTO queryDTO) {
        if (queryDTO.getUserId() == null) {
            throw new ServiceException("用户ID不能为空");
        }

        Page<ServiceAppointment> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize());
        LambdaQueryWrapper<ServiceAppointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceAppointment::getUserId, queryDTO.getUserId());

        if (StringUtils.isNotBlank(queryDTO.getStatus())) {
            queryWrapper.eq(ServiceAppointment::getStatus, queryDTO.getStatus());
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(ServiceAppointment::getCreateTime);

        // 执行分页查询
        Page<ServiceAppointment> resultPage = appointmentMapper.selectPage(page, queryWrapper);

        // 转换为DTO
        Page<ServiceAppointmentDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(),
                resultPage.getTotal());
        dtoPage.setRecords(resultPage.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList()));

        return dtoPage;
    }

    /**
     * 分页查询所有预约列表（管理端）
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    public Page<ServiceAppointmentDTO> getAppointmentsByPage(AppointmentQueryDTO queryDTO) {
        Page<ServiceAppointment> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize());
        LambdaQueryWrapper<ServiceAppointment> queryWrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq(ServiceAppointment::getUserId, queryDTO.getUserId());
        }

        if (StringUtils.isNotBlank(queryDTO.getPetName())) {
            queryWrapper.like(ServiceAppointment::getPetName, queryDTO.getPetName());
        }

        if (StringUtils.isNotBlank(queryDTO.getContactPhone())) {
            queryWrapper.like(ServiceAppointment::getContactPhone, queryDTO.getContactPhone());
        }

        if (StringUtils.isNotBlank(queryDTO.getStatus())) {
            queryWrapper.eq(ServiceAppointment::getStatus, queryDTO.getStatus());
        }

        // 处理日期范围查询
        if (StringUtils.isNotBlank(queryDTO.getStartDate()) && StringUtils.isNotBlank(queryDTO.getEndDate())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime startDate = LocalDate.parse(queryDTO.getStartDate(), formatter).atStartOfDay();
            LocalDateTime endDate = LocalDate.parse(queryDTO.getEndDate(), formatter).plusDays(1).atStartOfDay();
            queryWrapper.between(ServiceAppointment::getAppointmentTime, startDate, endDate);
        }

        // 处理服务名称查询（需要联表查询，这里简化处理）
        if (StringUtils.isNotBlank(queryDTO.getServiceName())) {
            // 先查询服务ID
            LambdaQueryWrapper<Service> serviceWrapper = new LambdaQueryWrapper<>();
            serviceWrapper.like(Service::getName, queryDTO.getServiceName());
            List<Service> services = serviceMapper.selectList(serviceWrapper);
            if (services != null && !services.isEmpty()) {
                List<Long> serviceIds = services.stream().map(Service::getId).collect(Collectors.toList());
                queryWrapper.in(ServiceAppointment::getServiceId, serviceIds);
            } else {
                // 如果没有符合条件的服务，直接返回空结果
                Page<ServiceAppointmentDTO> emptyPage = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize(), 0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(ServiceAppointment::getCreateTime);

        // 执行分页查询
        Page<ServiceAppointment> resultPage = appointmentMapper.selectPage(page, queryWrapper);

        // 转换为DTO
        Page<ServiceAppointmentDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(),
                resultPage.getTotal());
        dtoPage.setRecords(resultPage.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList()));

        return dtoPage;
    }

    /**
     * 取消预约
     *
     * @param id     预约ID
     * @param userId 用户ID
     * @return 操作结果
     */
    public boolean cancelAppointment(Long id, Long userId) {
        if (id == null) {
            throw new ServiceException("预约ID不能为空");
        }

        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        ServiceAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new ServiceException("预约不存在");
        }

        // 检查权限（只有预约人自己可以取消）
        if (!appointment.getUserId().equals(userId)) {
            throw new ServiceException("无权操作此预约");
        }

        // 只有"已预约"和"已确认"状态的预约可以取消
        if (!"已预约".equals(appointment.getStatus()) && !"已确认".equals(appointment.getStatus())) {
            throw new ServiceException("当前状态无法取消预约");
        }

        // 更新状态为已取消
        appointment.setStatus("已取消");
        int result = appointmentMapper.updateById(appointment);

        return result > 0;
    }

    /**
     * 更新预约状态（管理端）
     *
     * @param id  预约ID
     * @param dto 包含状态更新信息的DTO
     * @return 操作结果
     */
    public boolean updateAppointmentStatus(Long id, ServiceAppointmentDTO dto) {
        if (id == null) {
            throw new ServiceException("预约ID不能为空");
        }

        if (dto == null || StringUtils.isBlank(dto.getStatus())) {
            throw new ServiceException("状态不能为空");
        }

        if (dto.getOperatorId() == null) {
            throw new ServiceException("操作人ID不能为空");
        }

        ServiceAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new ServiceException("预约不存在");
        }

        // 检查状态变更是否有效
        if (!isValidStatusChange(appointment.getStatus(), dto.getStatus())) {
            throw new ServiceException("无效的状态变更");
        }

        // 更新状态
        appointment.setStatus(dto.getStatus());
        int result = appointmentMapper.updateById(appointment);

        return result > 0;
    }

    /**
     * 根据状态获取操作描述
     */
    private String getActionByStatus(String status) {
        switch (status) {
            case "已确认":
                return "确认预约";
            case "已完成":
                return "完成服务";
            case "已取消":
                return "取消预约";
            default:
                return "更新状态";
        }
    }

    /**
     * 检查状态变更是否有效
     */
    private boolean isValidStatusChange(String currentStatus, String newStatus) {
        switch (currentStatus) {
            case "已预约":
                return "已确认".equals(newStatus) || "已取消".equals(newStatus);
            case "已确认":
                return "已完成".equals(newStatus) || "已取消".equals(newStatus);
            case "已完成":
            case "已取消":
                return false; // 终态不能再变更
            default:
                return false;
        }
    }
}