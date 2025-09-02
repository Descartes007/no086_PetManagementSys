package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.DTO.ServiceQueryDTO;
import org.example.springboot.entity.Service;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * 服务业务类
 */
@org.springframework.stereotype.Service
public class ServiceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceService.class);

    @Resource
    private ServiceMapper serviceMapper;

    /**
     * 分页查询服务列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    public Page<Service> getServicesByPage(ServiceQueryDTO queryDTO) {
        LambdaQueryWrapper<Service> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (StringUtils.isNotBlank(queryDTO.getName())) {
            queryWrapper.like(Service::getName, queryDTO.getName());
        }
        
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq(Service::getCategoryId, queryDTO.getCategoryId());
        }
        
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Service::getStatus, queryDTO.getStatus());
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Service::getCreateTime);
        
        // 执行分页查询
        Page<Service> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize());
        return serviceMapper.selectPage(page, queryWrapper);
    }

    /**
     * 根据ID获取服务详情
     *
     * @param id 服务ID
     * @return 服务详情
     */
    public Service getServiceById(Long id) {
        Service service = serviceMapper.selectById(id);
        if (service == null) {
            throw new ServiceException("服务不存在");
        }
        return service;
    }

    /**
     * 添加服务
     *
     * @param service 服务信息
     * @return 添加结果
     */
    public boolean addService(Service service) {
        if (service == null) {
            throw new ServiceException("服务信息不能为空");
        }
        
        // 设置初始状态为启用
        if (service.getStatus() == null) {
            service.setStatus(1);
        }
        
        validateService(service);
        
        int result = serviceMapper.insert(service);
        return result > 0;
    }

    /**
     * 更新服务
     *
     * @param service 服务信息
     * @return 更新结果
     */
    public boolean updateService(Service service) {
        if (service == null || service.getId() == null) {
            throw new ServiceException("服务信息不完整");
        }
        
        // 检查服务是否存在
        Service existingService = getServiceById(service.getId());
        if (existingService == null) {
            throw new ServiceException("服务不存在");
        }
        
        validateService(service);
        
        int result = serviceMapper.updateById(service);
        return result > 0;
    }

    /**
     * 更新服务状态
     *
     * @param id     服务ID
     * @param status 状态
     * @return 更新结果
     */
    public boolean updateServiceStatus(Long id, Integer status) {
        if (id == null) {
            throw new ServiceException("服务ID不能为空");
        }
        
        if (status == null || (status != 0 && status != 1)) {
            throw new ServiceException("状态值无效");
        }
        
        // 检查服务是否存在
        Service existingService = getServiceById(id);
        if (existingService == null) {
            throw new ServiceException("服务不存在");
        }
        
        // 设置新状态
        existingService.setStatus(status);
        
        int result = serviceMapper.updateById(existingService);
        return result > 0;
    }

    /**
     * 删除服务
     *
     * @param id 服务ID
     * @return 删除结果
     */
    public boolean deleteService(Long id) {
        if (id == null) {
            throw new ServiceException("服务ID不能为空");
        }
        
        // 检查服务是否存在
        Service existingService = getServiceById(id);
        if (existingService == null) {
            throw new ServiceException("服务不存在");
        }
        
        int result = serviceMapper.deleteById(id);
        return result > 0;
    }

    /**
     * 验证服务信息
     *
     * @param service 服务信息
     */
    private void validateService(Service service) {
        if (StringUtils.isBlank(service.getName())) {
            throw new ServiceException("服务名称不能为空");
        }
        
        if (service.getCategoryId() == null) {
            throw new ServiceException("服务分类不能为空");
        }
        
        if (service.getPrice() == null) {
            throw new ServiceException("服务价格不能为空");
        }
        
        if (service.getPrice().doubleValue() < 0) {
            throw new ServiceException("服务价格不能为负数");
        }
        
        if (service.getDuration() == null) {
            throw new ServiceException("服务时长不能为空");
        }
        
        if (service.getDuration() <= 0) {
            throw new ServiceException("服务时长必须大于0");
        }
    }

    /**
     * 搜索服务
     *
     * @param keyword 关键词
     * @param currentPage 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    public Page<Service> searchServices(String keyword, Integer currentPage, Integer size) {
        if (StringUtils.isBlank(keyword)) {
            return new Page<>(currentPage, size);
        }
        
        LambdaQueryWrapper<Service> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Service::getName, keyword)
                .or()
                .like(Service::getDescription, keyword);
        
        // 只搜索启用状态的服务
        queryWrapper.eq(Service::getStatus, 1);
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Service::getCreateTime);
        
        // 执行分页查询
        Page<Service> page = new Page<>(currentPage, size);
        return serviceMapper.selectPage(page, queryWrapper);
    }
} 