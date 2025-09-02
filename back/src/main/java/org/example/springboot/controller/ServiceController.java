package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.ServiceQueryDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Service;
import org.example.springboot.service.ServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


/**
 * 服务管理接口
 */
@Tag(name = "服务管理接口")
@RestController
@RequestMapping("/service")
public class ServiceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceController.class);

    @Resource
    private ServiceService serviceService;

    @Operation(summary = "分页查询服务列表")
    @GetMapping("/page")
    public Result<?> getServicesByPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("分页查询服务列表: name={}, categoryId={}, status={}, currentPage={}, size={}", 
                name, categoryId, status, currentPage, size);
        
        ServiceQueryDTO queryDTO = new ServiceQueryDTO();
        queryDTO.setName(name);
        queryDTO.setCategoryId(categoryId);
        queryDTO.setStatus(status);
        queryDTO.setCurrentPage(currentPage);
        queryDTO.setSize(size);
        
        Page<Service> page = serviceService.getServicesByPage(queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "根据ID获取服务详情")
    @GetMapping("/{id}")
    public Result<?> getServiceById(@PathVariable Long id) {
        LOGGER.info("获取服务详情: id={}", id);
        Service service = serviceService.getServiceById(id);
        return Result.success(service);
    }

    @Operation(summary = "添加服务")
    @PostMapping
    public Result<?> addService(@RequestBody Service service) {
        LOGGER.info("添加服务: {}", service);
        boolean success = serviceService.addService(service);
        return success ? Result.success(service.getId()) : Result.error("添加服务失败");
    }

    @Operation(summary = "更新服务")
    @PutMapping("/{id}")
    public Result<?> updateService(@PathVariable Long id, @RequestBody Service service) {
        LOGGER.info("更新服务: id={}, service={}", id, service);
        service.setId(id);
        boolean success = serviceService.updateService(service);
        return success ? Result.success() : Result.error("更新服务失败");
    }

    @Operation(summary = "更新服务状态")
    @PutMapping("/{id}/status")
    public Result<?> updateServiceStatus(@PathVariable Long id, @RequestParam Integer status) {
        LOGGER.info("更新服务状态: id={}, status={}", id, status);
        boolean success = serviceService.updateServiceStatus(id, status);
        return success ? Result.success() : Result.error("更新服务状态失败");
    }

    @Operation(summary = "删除服务")
    @DeleteMapping("/{id}")
    public Result<?> deleteService(@PathVariable Long id) {
        LOGGER.info("删除服务: id={}", id);
        boolean success = serviceService.deleteService(id);
        return success ? Result.success() : Result.error("删除服务失败");
    }
} 