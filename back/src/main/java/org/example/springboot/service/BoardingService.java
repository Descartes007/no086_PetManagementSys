package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.entity.Boarding;
import org.example.springboot.entity.User;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.BoardingMapper;
import org.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

/**
 * 寄养信息服务类
 */
@Service
public class BoardingService {
    @Resource
    private BoardingMapper boardingMapper;
    
    @Resource
    private UserMapper userMapper;

    /**
     * 分页查询寄养信息
     *
     * @param petName     宠物名称（模糊查询）
     * @param petType     宠物类型
     * @param status      寄养状态
     * @param currentPage 当前页
     * @param size        每页数量
     * @return 分页结果
     */
    public Page<Boarding> getBoardingsByPage(String petName, String petType, String status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Boarding> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(petName)) {
            queryWrapper.like(Boarding::getPetName, petName);
        }
        if (StringUtils.isNotBlank(petType)) {
            queryWrapper.eq(Boarding::getPetType, petType);
        }
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(Boarding::getStatus, status);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Boarding::getCreateTime);
        
        // 执行分页查询
        Page<Boarding> page = boardingMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        
        // 填充用户名
        for (Boarding boarding : page.getRecords()) {
            if (boarding.getUserId() != null) {
                User user = userMapper.selectById(boarding.getUserId());
                if (user != null) {
                    boarding.setUserName(user.getUsername());
                }
            }
        }
        
        return page;
    }

    private void fillRelated(Boarding boarding) {
        if (boarding.getUserId() != null) {
            User user = userMapper.selectById(boarding.getUserId());
            if (user != null) {
                boarding.setUserName(user.getUsername());
            }
        }
//        if()
    }

    /**
     * 根据用户ID分页查询寄养信息
     *
     * @param userId      用户ID
     * @param status      寄养状态
     * @param currentPage 当前页
     * @param size        每页数量
     * @return 分页结果
     */
    public Page<Boarding> getBoardingsByUserId(Long userId, String status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Boarding> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加用户ID条件
        queryWrapper.eq(Boarding::getUserId, userId);
        
        // 添加状态条件
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(Boarding::getStatus, status);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Boarding::getCreateTime);
        
        // 执行分页查询
        return boardingMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }

    /**
     * 根据ID获取寄养信息
     *
     * @param id 寄养ID
     * @return 寄养信息
     */
    public Boarding getBoardingById(Long id) {
        Boarding boarding = boardingMapper.selectById(id);
        if (boarding == null) {
            throw new ServiceException("寄养信息不存在");
        }
        
        // 填充用户信息
        if (boarding.getUserId() != null) {
            User user = userMapper.selectById(boarding.getUserId());
            if (user != null) {
                boarding.setUserName(user.getUsername());
            }
        }
        
        return boarding;
    }

    /**
     * 添加寄养信息
     *
     * @param boarding 寄养信息
     * @return 添加后的寄养信息
     */
    @Transactional
    public Boarding addBoarding(Boarding boarding) {
        // 校验数据
        validateBoardingData(boarding);
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        boarding.setCreateTime(now);
        boarding.setUpdateTime(now);
        
        // 插入数据
        boardingMapper.insert(boarding);
        
        return boarding;
    }

    /**
     * 更新寄养信息
     *
     * @param boarding 寄养信息
     * @return 更新后的寄养信息
     */
    @Transactional
    public Boarding updateBoarding(Boarding boarding) {
        // 校验ID
        if (boarding.getId() == null) {
            throw new ServiceException("寄养ID不能为空");
        }
        
        // 校验寄养信息是否存在
        Boarding existingBoarding = boardingMapper.selectById(boarding.getId());
        if (existingBoarding == null) {
            throw new ServiceException("寄养信息不存在");
        }
        
        // 校验数据
        validateBoardingData(boarding);
        
        // 设置更新时间
        boarding.setUpdateTime(LocalDateTime.now());
        
        // 更新数据
        boardingMapper.updateById(boarding);
        
        return boardingMapper.selectById(boarding.getId());
    }

    /**
     * 更新寄养状态
     *
     * @param id     寄养ID
     * @param status 新状态
     * @return 更新后的寄养信息
     */
    @Transactional
    public Boarding updateBoardingStatus(Long id, String status) {
        // 校验状态
        if (StringUtils.isBlank(status)) {
            throw new ServiceException("状态不能为空");
        }
        
        // 检查状态值是否合法
        if (!isValidStatus(status)) {
            throw new ServiceException("无效的状态值");
        }
        
        // 校验寄养信息是否存在
        Boarding boarding = boardingMapper.selectById(id);
        if (boarding == null) {
            throw new ServiceException("寄养信息不存在");
        }
        
        // 更新状态
        boarding.setStatus(status);
        boarding.setUpdateTime(LocalDateTime.now());
        boardingMapper.updateById(boarding);
        
        return boarding;
    }

    /**
     * 取消寄养申请
     *
     * @param id 寄养ID
     * @return 更新后的寄养信息
     */
    @Transactional
    public Boarding cancelBoarding(Long id) {
        // 校验寄养信息是否存在
        Boarding boarding = boardingMapper.selectById(id);
        if (boarding == null) {
            throw new ServiceException("寄养信息不存在");
        }
        
        // 只有"已申请"状态的寄养可以取消
        if (!"已申请".equals(boarding.getStatus())) {
            throw new ServiceException("只有待处理的寄养申请可以取消");
        }
        
        // 更新状态为"已取消"
        boarding.setStatus("已取消");
        boarding.setUpdateTime(LocalDateTime.now());
        boardingMapper.updateById(boarding);
        
        return boarding;
    }

    /**
     * 删除寄养信息
     *
     * @param id 寄养ID
     */
    @Transactional
    public void deleteBoarding(Long id) {
        // 校验寄养信息是否存在
        Boarding boarding = boardingMapper.selectById(id);
        if (boarding == null) {
            throw new ServiceException("寄养信息不存在");
        }
        
        // 删除寄养信息
        boardingMapper.deleteById(id);
    }

    /**
     * 校验寄养数据
     *
     * @param boarding 寄养信息
     */
    private void validateBoardingData(Boarding boarding) {
        // 校验宠物名称
        if (StringUtils.isBlank(boarding.getPetName())) {
            throw new ServiceException("宠物名称不能为空");
        }
        
        // 校验宠物类型
        if (StringUtils.isBlank(boarding.getPetType())) {
            throw new ServiceException("宠物类型不能为空");
        }
        
        // 校验开始时间
        if (boarding.getStartTime() == null) {
            throw new ServiceException("开始时间不能为空");
        }
        
        // 校验结束时间
        if (boarding.getEndTime() == null) {
            throw new ServiceException("结束时间不能为空");
        }
        
        // 校验结束时间必须大于开始时间
        if (boarding.getEndTime().isBefore(boarding.getStartTime())) {
            throw new ServiceException("结束时间必须大于开始时间");
        }
        
        // 校验状态
        if (StringUtils.isBlank(boarding.getStatus())) {
            throw new ServiceException("状态不能为空");
        }
        
        // 检查状态值是否合法
        if (!isValidStatus(boarding.getStatus())) {
            throw new ServiceException("无效的状态值");
        }
    }

    /**
     * 检查状态值是否合法
     *
     * @param status 状态值
     * @return 是否合法
     */
    private boolean isValidStatus(String status) {
        return "已申请".equals(status) || "已接受".equals(status) || "进行中".equals(status) 
                || "已完成".equals(status) || "已取消".equals(status);
    }
} 