package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Announcement;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.service.AnnouncementService;
import org.example.springboot.util.JwtTokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告管理接口
 */
@Tag(name = "公告管理接口")
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnouncementController.class);

    @Resource
    private AnnouncementService announcementService;

    /**
     * 分页获取公告列表
     */
    @Operation(summary = "分页获取公告列表")
    @GetMapping("/page")
    public Result<?> getAnnouncementsByPage(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Announcement> page = announcementService.getAnnouncementsByPage(title, type, status, currentPage, size);
        return Result.success(page);
    }

    /**
     * 获取所有启用的公告
     */
    @Operation(summary = "获取所有启用的公告")
    @GetMapping("/list")
    public Result<?> getEnabledAnnouncements() {
        List<Announcement> announcements = announcementService.getEnabledAnnouncements();
        return Result.success(announcements);
    }

    /**
     * 获取最新的几条启用的公告
     */
    @Operation(summary = "获取最新的几条启用的公告")
    @GetMapping("/latest")
    public Result<?> getLatestAnnouncements(@RequestParam(defaultValue = "5") Integer limit) {
        List<Announcement> announcements = announcementService.getLatestAnnouncements(limit);
        return Result.success(announcements);
    }

    /**
     * 根据ID获取公告
     */
    @Operation(summary = "根据ID获取公告")
    @GetMapping("/{id}")
    public Result<?> getAnnouncementById(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        if (announcement == null) {
            throw new ServiceException("公告不存在");
        }
        return Result.success(announcement);
    }

    /**
     * 添加公告
     */
    @Operation(summary = "添加公告")
    @PostMapping
    public Result<?> addAnnouncement(@RequestBody Announcement announcement) {
        // 获取当前登录用户ID
        User currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("用户未登录！");
        }

        announcement.setCreatedBy(currentUser.getId());
        
        boolean success = announcementService.addAnnouncement(announcement);
        if (!success) {
            throw new ServiceException("添加公告失败");
        }
        return Result.success();
    }

    /**
     * 更新公告
     */
    @Operation(summary = "更新公告")
    @PutMapping("/{id}")
    public Result<?> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        boolean success = announcementService.updateAnnouncement(announcement);
        if (!success) {
            throw new ServiceException("更新公告失败");
        }
        return Result.success();
    }

    /**
     * 删除公告
     */
    @Operation(summary = "删除公告")
    @DeleteMapping("/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        boolean success = announcementService.deleteAnnouncement(id);
        if (!success) {
            throw new ServiceException("删除公告失败");
        }
        return Result.success();
    }

    /**
     * 更新公告状态
     */
    @Operation(summary = "更新公告状态")
    @PutMapping("/{id}/status/{status}")
    public Result<?> updateAnnouncementStatus(@PathVariable Long id, @PathVariable Integer status) {
        if (status != 0 && status != 1) {
            throw new ServiceException("状态值无效");
        }
        boolean success = announcementService.updateAnnouncementStatus(id, status);
        if (!success) {
            throw new ServiceException("更新公告状态失败");
        }
        return Result.success();
    }
} 