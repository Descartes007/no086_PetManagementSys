package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Banner;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.service.BannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * 轮播图管理接口
 */
@Tag(name = "轮播图管理接口")
@RestController
@RequestMapping("/banner")
public class BannerController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BannerController.class);

    @Resource
    private BannerService bannerService;

    /**
     * 分页获取轮播图列表
     */
    @Operation(summary = "分页获取轮播图列表")
    @GetMapping("/page")
    public Result<?> getBannersByPage(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Banner> page = bannerService.getBannersByPage(title, status, currentPage, size);
        return Result.success(page);
    }

    /**
     * 获取所有启用的轮播图
     */
    @Operation(summary = "获取所有启用的轮播图")
    @GetMapping("/list")
    public Result<?> getEnabledBanners() {
        List<Banner> banners = bannerService.getEnabledBanners();
        return Result.success(banners);
    }

    /**
     * 根据ID获取轮播图
     */
    @Operation(summary = "根据ID获取轮播图")
    @GetMapping("/{id}")
    public Result<?> getBannerById(@PathVariable Long id) {
        Banner banner = bannerService.getBannerById(id);
        if (banner == null) {
            throw new ServiceException("轮播图不存在");
        }
        return Result.success(banner);
    }

    /**
     * 添加轮播图
     */
    @Operation(summary = "添加轮播图")
    @PostMapping
    public Result<?> addBanner(@RequestBody Banner banner) {
        if (banner.getImageUrl() == null || banner.getImageUrl().isEmpty()) {
            throw new ServiceException("图片URL不能为空");
        }
        boolean success = bannerService.addBanner(banner);
        if (!success) {
            throw new ServiceException("添加轮播图失败");
        }
        return Result.success();
    }

    /**
     * 更新轮播图
     */
    @Operation(summary = "更新轮播图")
    @PutMapping("/{id}")
    public Result<?> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        boolean success = bannerService.updateBanner(banner);
        if (!success) {
            throw new ServiceException("更新轮播图失败");
        }
        return Result.success();
    }

    /**
     * 删除轮播图
     */
    @Operation(summary = "删除轮播图")
    @DeleteMapping("/{id}")
    public Result<?> deleteBanner(@PathVariable Long id) {
        boolean success = bannerService.deleteBanner(id);
        if (!success) {
            throw new ServiceException("删除轮播图失败");
        }
        return Result.success();
    }

    /**
     * 更新轮播图状态
     */
    @Operation(summary = "更新轮播图状态")
    @PutMapping("/{id}/status/{status}")
    public Result<?> updateBannerStatus(@PathVariable Long id, @PathVariable Integer status) {
        if (status != 0 && status != 1) {
            throw new ServiceException("状态值无效");
        }
        boolean success = bannerService.updateBannerStatus(id, status);
        if (!success) {
            throw new ServiceException("更新轮播图状态失败");
        }
        return Result.success();
    }
} 