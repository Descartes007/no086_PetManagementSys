package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Banner;
import org.example.springboot.mapper.BannerMapper;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图服务类
 */
@Service
public class BannerService {

    @Resource
    private BannerMapper bannerMapper;

    /**
     * 分页获取轮播图列表
     *
     * @param title      标题
     * @param status     状态
     * @param currentPage 当前页
     * @param size       每页大小
     * @return 分页结果
     */
    public Page<Banner> getBannersByPage(String title, Integer status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (title != null && !title.isEmpty()) {
            queryWrapper.like(Banner::getTitle, title);
        }
        if (status != null) {
            queryWrapper.eq(Banner::getStatus, status);
        }
        
        // 按排序字段排序，再按ID排序
        queryWrapper.orderByAsc(Banner::getSort);
        queryWrapper.orderByDesc(Banner::getId);
        
        return bannerMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }

    /**
     * 获取所有启用的轮播图，按排序字段排序
     *
     * @return 轮播图列表
     */
    public List<Banner> getEnabledBanners() {
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Banner::getStatus, 1);
        queryWrapper.orderByAsc(Banner::getSort);
        queryWrapper.orderByDesc(Banner::getId);
        return bannerMapper.selectList(queryWrapper);
    }

    /**
     * 根据ID获取轮播图
     *
     * @param id 轮播图ID
     * @return 轮播图信息
     */
    public Banner getBannerById(Long id) {
        return bannerMapper.selectById(id);
    }

    /**
     * 添加轮播图
     *
     * @param banner 轮播图信息
     * @return 操作结果
     */
    public boolean addBanner(Banner banner) {
        banner.setCreateTime(LocalDateTime.now());
        banner.setUpdateTime(LocalDateTime.now());
        return bannerMapper.insert(banner) > 0;
    }

    /**
     * 更新轮播图
     *
     * @param banner 轮播图信息
     * @return 操作结果
     */
    public boolean updateBanner(Banner banner) {
        banner.setUpdateTime(LocalDateTime.now());
        return bannerMapper.updateById(banner) > 0;
    }

    /**
     * 删除轮播图
     *
     * @param id 轮播图ID
     * @return 操作结果
     */
    public boolean deleteBanner(Long id) {
        return bannerMapper.deleteById(id) > 0;
    }

    /**
     * 更新轮播图状态
     *
     * @param id     轮播图ID
     * @param status 状态
     * @return 操作结果
     */
    public boolean updateBannerStatus(Long id, Integer status) {
        Banner banner = new Banner();
        banner.setId(id);
        banner.setStatus(status);
        banner.setUpdateTime(LocalDateTime.now());
        return bannerMapper.updateById(banner) > 0;
    }
} 