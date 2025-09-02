package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.DTO.AdoptionDTO;
import org.example.springboot.entity.Adoption;
import org.example.springboot.entity.Pet;
import org.example.springboot.entity.User;
import org.example.springboot.enumClass.AdoptionStatus;
import org.example.springboot.enumClass.ApplicationStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.AdoptionMapper;
import org.example.springboot.mapper.PetMapper;
import org.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdoptionService {
    
    @Resource
    private AdoptionMapper adoptionMapper;
    
    @Resource
    private PetMapper petMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private PetService petService;
    
    /**
     * 分页查询领养申请
     */
    public Page<Adoption> getAdoptionsByPage(Long userId, Long petId, String status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Adoption> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (userId != null) {
            queryWrapper.eq(Adoption::getUserId, userId);
        }
        if (petId != null) {
            queryWrapper.eq(Adoption::getPetId, petId);
        }
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(Adoption::getStatus, status);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Adoption::getCreateTime);
        
        return adoptionMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }
    
    /**
     * 获取领养申请详情
     */
    public Adoption getAdoptionById(Long id) {
        Adoption adoption = adoptionMapper.selectById(id);
        if (adoption == null) {
            throw new ServiceException("申请不存在");
        }
        return adoption;
    }
    
    /**
     * 创建领养申请
     */
    @Transactional
    public void createAdoption(Long userId, AdoptionDTO adoptionDTO) {
        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 验证宠物是否存在
        Pet pet = petMapper.selectById(adoptionDTO.getPetId());
        if (pet == null) {
            throw new ServiceException("宠物不存在");
        }
        
        // 验证宠物是否可领养
        if (!AdoptionStatus.AVAILABLE.getValue().equals(pet.getAdoptionStatus())) {
            throw new ServiceException("该宠物当前不可领养");
        }
        
        // 检查用户是否已经申请过该宠物
        LambdaQueryWrapper<Adoption> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Adoption::getUserId, userId);
        queryWrapper.eq(Adoption::getPetId, adoptionDTO.getPetId());
        Adoption existAdoption = adoptionMapper.selectOne(queryWrapper);
        
        if (existAdoption != null) {
            throw new ServiceException("您已经申请过该宠物，请勿重复申请");
        }
        
        // 创建申请
        Adoption adoption = new Adoption();
        adoption.setUserId(userId);
        adoption.setPetId(adoptionDTO.getPetId());
        adoption.setStatus(ApplicationStatus.APPLIED.getValue());
        adoption.setApplyReason(adoptionDTO.getApplyReason());
        adoption.setContactPhone(adoptionDTO.getContactPhone());
        adoption.setAddress(adoptionDTO.getAddress());
        adoption.setCreateTime(LocalDateTime.now());
        adoption.setUpdateTime(LocalDateTime.now());
        
        adoptionMapper.insert(adoption);
        
        // 不再改变宠物状态，保持为"可领养"状态直到申请被通过
    }
    
    /**
     * 取消领养申请
     */
    @Transactional
    public void cancelAdoption(Long id, Long userId) {
        Adoption adoption = getAdoptionById(id);
        
        // 验证是否是本人操作
        if (!adoption.getUserId().equals(userId)) {
            throw new ServiceException("只能取消自己的申请");
        }
        
        // 验证状态是否可取消
        if (ApplicationStatus.APPROVED.getValue().equals(adoption.getStatus()) || 
            ApplicationStatus.REJECTED.getValue().equals(adoption.getStatus())) {
            throw new ServiceException("此申请已经处理完成，无法取消");
        }
        
        // 删除申请，不需要处理宠物状态，因为宠物一直是"可领养"状态
        adoptionMapper.deleteById(id);
    }
    
    /**
     * 审核领养申请
     */
    @Transactional
    public void reviewAdoption(Long id, String status) {
        Adoption adoption = getAdoptionById(id);
        
        // 更新申请状态
        adoption.setStatus(status);
        adoption.setUpdateTime(LocalDateTime.now());
        adoptionMapper.updateById(adoption);
        
        Pet pet = petMapper.selectById(adoption.getPetId());
        if (pet == null) {
            throw new ServiceException("宠物不存在");
        }
        
        // 根据审核结果更新宠物状态
        if (ApplicationStatus.APPROVED.getValue().equals(status)) {
            pet.setAdoptionStatus(AdoptionStatus.ADOPTED.getValue());
            petMapper.updateById(pet);
            
            // 拒绝其他申请
            rejectOtherApplications(pet.getId(), adoption.getId());
        }
        // 如果申请被拒绝，宠物状态保持为"可领养"
    }
    
    /**
     * 拒绝其他申请
     */
    private void rejectOtherApplications(Long petId, Long approvedAdoptionId) {
        LambdaQueryWrapper<Adoption> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Adoption::getPetId, petId);
        queryWrapper.ne(Adoption::getId, approvedAdoptionId);
        queryWrapper.ne(Adoption::getStatus, ApplicationStatus.REJECTED.getValue());
        
        Adoption updateAdoption = new Adoption();
        updateAdoption.setStatus(ApplicationStatus.REJECTED.getValue());
        updateAdoption.setUpdateTime(LocalDateTime.now());
        
        adoptionMapper.update(updateAdoption, queryWrapper);
    }
    
    /**
     * 获取用户的申请记录
     */
    public Page<Adoption> getUserAdoptions(Long userId, String status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Adoption> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Adoption::getUserId, userId);
        
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(Adoption::getStatus, status);
        }
        
        queryWrapper.orderByDesc(Adoption::getCreateTime);
        
        return adoptionMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }
} 