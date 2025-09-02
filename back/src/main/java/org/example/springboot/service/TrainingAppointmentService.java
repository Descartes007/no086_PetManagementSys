package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.DTO.TrainingAppointmentDTO;
import org.example.springboot.DTO.TrainingQueryDTO;
import org.example.springboot.entity.TrainingAppointment;
import org.example.springboot.entity.TrainingCategory;
import org.example.springboot.entity.TrainingCourse;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.TrainingAppointmentMapper;
import org.example.springboot.mapper.TrainingCategoryMapper;
import org.example.springboot.mapper.TrainingCourseMapper;
import org.example.springboot.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 训练预约服务类
 */
@Service
public class TrainingAppointmentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingAppointmentService.class);

    @Resource
    private TrainingAppointmentMapper appointmentMapper;

    @Resource
    private TrainingCourseMapper courseMapper;

    @Resource
    private UserMapper userMapper;
    @Autowired
    private TrainingCategoryMapper trainingCategoryMapper;

    /**
     * 创建预约
     *
     * @param appointment 预约信息
     * @return 预约ID
     */
    @Transactional
    public Long createAppointment(TrainingAppointment appointment) {
        if (appointment == null) {
            throw new ServiceException("预约信息不能为空");
        }

        validateAppointment(appointment);

        // 设置初始状态为"已预约"
        appointment.setStatus("已预约");

        // 设置初始进度为0
        if (appointment.getProgress() == null) {
            appointment.setProgress(0);
        }

        appointment.setCreateTime(LocalDateTime.now());

        int result = appointmentMapper.insert(appointment);
        if (result <= 0) {
            throw new ServiceException("创建预约失败");
        }

        return appointment.getId();
    }

    /**
     * 验证预约信息
     */
    private void validateAppointment(TrainingAppointment appointment) {
        if (appointment.getUserId() == null) {
            throw new ServiceException("用户ID不能为空");
        }

        if (appointment.getCourseId() == null) {
            throw new ServiceException("课程ID不能为空");
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

        // 验证课程是否存在且状态正常
        TrainingCourse course = courseMapper.selectById(appointment.getCourseId());
        if (course == null) {
            throw new ServiceException("所选课程不存在");
        }

        if (course.getStatus() != 1) {
            throw new ServiceException("所选课程暂不可预约");
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
    public TrainingAppointmentDTO getAppointmentById(Long id) {
        if (id == null) {
            throw new ServiceException("预约ID不能为空");
        }

        TrainingAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new ServiceException("预约不存在");
        }

        return convertToDTO(appointment);
    }

    /**
     * 将实体转换为DTO
     */
    private TrainingAppointmentDTO convertToDTO(TrainingAppointment appointment) {
        TrainingAppointmentDTO dto = new TrainingAppointmentDTO();
        dto.setId(appointment.getId());
        dto.setUserId(appointment.getUserId());
        dto.setCourseId(appointment.getCourseId());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setPetName(appointment.getPetName());
        dto.setRequirements(appointment.getRequirements());
        dto.setProgress(appointment.getProgress());
        dto.setContactPhone(appointment.getContactPhone());
        dto.setCreateTime(appointment.getCreateTime());
        dto.setUpdateTime(appointment.getUpdateTime());

        // 设置反馈信息
        dto.setRating(appointment.getRating());
        dto.setFeedback(appointment.getFeedback());
        dto.setFeedbackTime(appointment.getFeedbackTime());
        dto.setHasFeedback(appointment.getFeedback() != null && !appointment.getFeedback().isEmpty());

        // 设置课程信息
        TrainingCourse course = courseMapper.selectById(appointment.getCourseId());
        if (course != null) {
            dto.setCourseName(course.getName());
            dto.setCategory(course.getCategory());
        }

        // 设置用户信息
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
    public Page<TrainingAppointmentDTO> getAppointmentsByUserId(TrainingQueryDTO queryDTO) {
        if (queryDTO.getUserId() == null) {
            throw new ServiceException("用户ID不能为空");
        }

        Page<TrainingAppointment> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize());
        LambdaQueryWrapper<TrainingAppointment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TrainingAppointment::getUserId, queryDTO.getUserId());

        if (StringUtils.isNotBlank(queryDTO.getStatus())) {
            queryWrapper.eq(TrainingAppointment::getStatus, queryDTO.getStatus());
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(TrainingAppointment::getCreateTime);

        // 执行分页查询
        Page<TrainingAppointment> resultPage = appointmentMapper.selectPage(page, queryWrapper);

        // 转换为DTO
        Page<TrainingAppointmentDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(),
                resultPage.getTotal());
        dtoPage.setRecords(resultPage.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList()));
        dtoPage.getRecords().forEach(this::fillCourse);
        return dtoPage;
    }

    /**
     * 分页查询所有预约列表（管理端）
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    public Page<TrainingAppointmentDTO> getAppointmentsByPage(TrainingQueryDTO queryDTO) {
        Page<TrainingAppointment> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize());
        LambdaQueryWrapper<TrainingAppointment> queryWrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq(TrainingAppointment::getUserId, queryDTO.getUserId());
        }

        if (StringUtils.isNotBlank(queryDTO.getPetName())) {
            queryWrapper.like(TrainingAppointment::getPetName, queryDTO.getPetName());
        }

        if (StringUtils.isNotBlank(queryDTO.getContactPhone())) {
            queryWrapper.like(TrainingAppointment::getContactPhone, queryDTO.getContactPhone());
        }

        if (StringUtils.isNotBlank(queryDTO.getStatus())) {
            queryWrapper.eq(TrainingAppointment::getStatus, queryDTO.getStatus());
        }

        // 处理日期范围查询
        if (StringUtils.isNotBlank(queryDTO.getStartDate()) && StringUtils.isNotBlank(queryDTO.getEndDate())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime startDate = LocalDate.parse(queryDTO.getStartDate(), formatter).atStartOfDay();
            LocalDateTime endDate = LocalDate.parse(queryDTO.getEndDate(), formatter).plusDays(1).atStartOfDay();
            queryWrapper.between(TrainingAppointment::getAppointmentTime, startDate, endDate);
        }

        // 处理课程名称查询（需要联表查询，这里简化处理）
        if (StringUtils.isNotBlank(queryDTO.getCourseName()) || queryDTO.getCourseId() != null) {
            LambdaQueryWrapper<TrainingCourse> courseWrapper = new LambdaQueryWrapper<>();
            if (StringUtils.isNotBlank(queryDTO.getCourseName())) {
                courseWrapper.like(TrainingCourse::getName, queryDTO.getCourseName());
            }
            if (queryDTO.getCourseId() != null) {
                courseWrapper.eq(TrainingCourse::getId, queryDTO.getCourseId());
            }

            List<TrainingCourse> courses = courseMapper.selectList(courseWrapper);
            if (courses != null && !courses.isEmpty()) {
                List<Long> courseIds = courses.stream().map(TrainingCourse::getId).collect(Collectors.toList());
                queryWrapper.in(TrainingAppointment::getCourseId, courseIds);
            } else {
                // 如果没有符合条件的课程，直接返回空结果
                Page<TrainingAppointmentDTO> emptyPage = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize(), 0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(TrainingAppointment::getCreateTime);

        // 执行分页查询
        Page<TrainingAppointment> resultPage = appointmentMapper.selectPage(page, queryWrapper);

        // 转换为DTO
        Page<TrainingAppointmentDTO> dtoPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(),
                resultPage.getTotal());
        dtoPage.setRecords(resultPage.getRecords().stream().map(this::convertToDTO).collect(Collectors.toList()));
        dtoPage.getRecords().forEach(this::fillCourse);

        return dtoPage;
    }

    /**
     * 取消预约
     *
     * @param id     预约ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public boolean cancelAppointment(Long id, Long userId) {
        if (id == null) {
            throw new ServiceException("预约ID不能为空");
        }

        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        TrainingAppointment appointment = appointmentMapper.selectById(id);
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
        appointment.setUpdateTime(LocalDateTime.now());

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
    @Transactional
    public boolean updateAppointmentStatus(Long id, TrainingAppointmentDTO dto) {
        if (id == null) {
            throw new ServiceException("预约ID不能为空");
        }

        if (dto == null || StringUtils.isBlank(dto.getStatus())) {
            throw new ServiceException("状态不能为空");
        }

        if (dto.getOperatorId() == null) {
            throw new ServiceException("操作人ID不能为空");
        }

        TrainingAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new ServiceException("预约不存在");
        }

        // 检查状态变更是否有效
        if (!isValidStatusChange(appointment.getStatus(), dto.getStatus())) {
            throw new ServiceException("无效的状态变更");
        }

        // 更新状态
        appointment.setStatus(dto.getStatus());

        // 更新进度
        if (dto.getProgress() != null) {
            appointment.setProgress(dto.getProgress());
        }

        appointment.setUpdateTime(LocalDateTime.now());

        int result = appointmentMapper.updateById(appointment);
        return result > 0;
    }

    /**
     * 检查状态变更是否有效
     */
    private boolean isValidStatusChange(String currentStatus, String newStatus) {
        switch (currentStatus) {
            case "已预约":
                return "已确认".equals(newStatus) || "已取消".equals(newStatus);
            case "已确认":
                return "进行中".equals(newStatus) || "已完成".equals(newStatus) || "已取消".equals(newStatus);
            case "进行中":
                return "已完成".equals(newStatus) || "已取消".equals(newStatus);
            case "已完成":
            case "已取消":
                return false; // 终态不能再变更
            default:
                return false;
        }
    }

    private void fillCourse(TrainingAppointmentDTO appointment) {
        if (appointment.getCourseId() != null) {
            TrainingCourse trainingCourse = courseMapper.selectById(appointment.getCourseId());
            if (trainingCourse != null) {
                if (trainingCourse.getCategoryId() != null) {
                    TrainingCategory category = trainingCategoryMapper.selectById(trainingCourse.getCategoryId());

                    appointment.setCategory(category.getName());
                }
                appointment.setCourseName(trainingCourse.getName());
                appointment.setCoursePrice(trainingCourse.getPrice());

            }
        }
    }

    /**
     * 更新训练进度
     *
     * @param id       预约ID
     * @param progress 进度(0-100)
     * @return 操作结果
     */
    @Transactional
    public boolean updateProgress(Long id, Integer progress, Long operatorId) {
        if (id == null) {
            throw new ServiceException("预约ID不能为空");
        }

        if (progress == null || progress < 0 || progress > 100) {
            throw new ServiceException("进度值无效，应在0-100之间");
        }

        if (operatorId == null) {
            throw new ServiceException("操作人ID不能为空");
        }

        TrainingAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new ServiceException("预约不存在");
        }

        // 只有已确认或进行中状态才能更新进度
        if (!"已确认".equals(appointment.getStatus()) && !"进行中".equals(appointment.getStatus())) {
            throw new ServiceException("只有已确认或进行中状态的预约才能更新进度");
        }

        // 如果进度达到100，自动更新状态为已完成
        if (progress == 100) {
            appointment.setStatus("已完成");
        }

        appointment.setProgress(progress);
        appointment.setUpdateTime(LocalDateTime.now());

        int result = appointmentMapper.updateById(appointment);
        return result > 0;
    }

    /**
     * 提交训练反馈
     *
     * @param id       预约ID
     * @param userId   用户ID
     * @param rating   满意度评分(1-5)
     * @param feedback 反馈内容
     * @return 操作结果
     */
    @Transactional
    public boolean submitFeedback(Long id, Long userId, Integer rating, String feedback) {
        if (id == null) {
            throw new ServiceException("预约ID不能为空");
        }

        if (userId == null) {
            throw new ServiceException("用户ID不能为空");
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new ServiceException("评分必须在1-5之间");
        }

        if (StringUtils.isBlank(feedback)) {
            throw new ServiceException("反馈内容不能为空");
        }

        TrainingAppointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new ServiceException("预约不存在");
        }

        // 检查权限（只有预约人自己可以提交反馈）
        if (!appointment.getUserId().equals(userId)) {
            throw new ServiceException("无权操作此预约");
        }

        // 只有"已完成"状态的预约可以提交反馈
        if (!"已完成".equals(appointment.getStatus())) {
            throw new ServiceException("只有已完成的训练才能提交反馈");
        }

        // 检查是否已提交过反馈
        if (appointment.getFeedback() != null && !appointment.getFeedback().isEmpty()) {
            throw new ServiceException("已提交过反馈，不能重复提交");
        }

        // 更新反馈信息
        appointment.setRating(rating);
        appointment.setFeedback(feedback);
        appointment.setFeedbackTime(LocalDateTime.now());
        appointment.setUpdateTime(LocalDateTime.now());

        int result = appointmentMapper.updateById(appointment);
        return result > 0;
    }
}