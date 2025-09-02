package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Announcement;
import org.example.springboot.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务类
 */
@Service
public class AnnouncementService {

    @Resource
    private AnnouncementMapper announcementMapper;

    /**
     * 分页获取公告列表
     *
     * @param title       标题
     * @param type        类型
     * @param status      状态
     * @param currentPage 当前页
     * @param size        每页大小
     * @return 分页结果
     */
    public Page<Announcement> getAnnouncementsByPage(String title, String type, Integer status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (title != null && !title.isEmpty()) {
            queryWrapper.like(Announcement::getTitle, title);
        }
        if (type != null && !type.isEmpty()) {
            queryWrapper.eq(Announcement::getType, type);
        }
        if (status != null) {
            queryWrapper.eq(Announcement::getStatus, status);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Announcement::getCreatedTime);
        
        return announcementMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }

    /**
     * 获取所有启用的公告
     *
     * @return 公告列表
     */
    public List<Announcement> getEnabledAnnouncements() {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Announcement::getStatus, 1);
        queryWrapper.orderByDesc(Announcement::getCreatedTime);
        return announcementMapper.selectList(queryWrapper);
    }

    /**
     * 获取最新的几条启用的公告
     *
     * @param limit 限制条数
     * @return 公告列表
     */
    public List<Announcement> getLatestAnnouncements(int limit) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Announcement::getStatus, 1);
        queryWrapper.orderByDesc(Announcement::getCreatedTime);
        queryWrapper.last("LIMIT " + limit);
        return announcementMapper.selectList(queryWrapper);
    }

    /**
     * 根据ID获取公告
     *
     * @param id 公告ID
     * @return 公告信息
     */
    public Announcement getAnnouncementById(Long id) {
        return announcementMapper.selectById(id);
    }

    /**
     * 添加公告
     *
     * @param announcement 公告信息
     * @return 操作结果
     */
    public boolean addAnnouncement(Announcement announcement) {
        announcement.setCreatedTime(LocalDateTime.now());
        announcement.setUpdatedTime(LocalDateTime.now());
        return announcementMapper.insert(announcement) > 0;
    }

    /**
     * 更新公告
     *
     * @param announcement 公告信息
     * @return 操作结果
     */
    public boolean updateAnnouncement(Announcement announcement) {
        announcement.setUpdatedTime(LocalDateTime.now());
        return announcementMapper.updateById(announcement) > 0;
    }

    /**
     * 删除公告
     *
     * @param id 公告ID
     * @return 操作结果
     */
    public boolean deleteAnnouncement(Long id) {
        return announcementMapper.deleteById(id) > 0;
    }

    /**
     * 更新公告状态
     *
     * @param id     公告ID
     * @param status 状态
     * @return 操作结果
     */
    public boolean updateAnnouncementStatus(Long id, Integer status) {
        Announcement announcement = new Announcement();
        announcement.setId(id);
        announcement.setStatus(status);
        announcement.setUpdatedTime(LocalDateTime.now());
        return announcementMapper.updateById(announcement) > 0;
    }
} 