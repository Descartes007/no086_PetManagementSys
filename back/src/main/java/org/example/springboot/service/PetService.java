package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.DTO.PetWithAdoptionStatusDTO;
import org.example.springboot.entity.Adoption;
import org.example.springboot.entity.Pet;
import org.example.springboot.entity.PetCategory;
import org.example.springboot.enumClass.AdoptionStatus;
import org.example.springboot.enumClass.ApplicationStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.AdoptionMapper;
import org.example.springboot.mapper.PetCategoryMapper;
import org.example.springboot.mapper.PetMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PetService {
    
    @Resource
    private PetMapper petMapper;
    
    @Resource
    private AdoptionMapper adoptionMapper;
    
    @Resource
    private PetCategoryMapper petCategoryMapper;
    
    @Resource
    private PetCategoryService petCategoryService;
    
    /**
     * 分页查询宠物列表
     */
    public Page<Pet> getPetsByPage(String name, String breed, String adoptionStatus, Long categoryId, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Pet> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(Pet::getName, name);
        }

        if (categoryId != null) {
            queryWrapper.eq(Pet::getCategoryId, categoryId);
        }
        if (StringUtils.isNotBlank(breed)) {
            queryWrapper.like(Pet::getBreed, breed);
        }
        if (StringUtils.isNotBlank(adoptionStatus)) {
            queryWrapper.eq(Pet::getAdoptionStatus, adoptionStatus);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Pet::getCreateTime);
        
        Page<Pet> pageResult = petMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        
        // 填充分类名称
        fillCategoryNames(pageResult.getRecords());
        
        return pageResult;
    }
    
    /**
     * 填充分类名称
     */
    private void fillCategoryNames(List<Pet> pets) {
        if (pets == null || pets.isEmpty()) {
            return;
        }
        
        // 获取所有分类ID并查询分类信息
        List<Long> categoryIds = pets.stream()
                .filter(pet -> pet.getCategoryId() != null)
                .map(Pet::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        
        if (!categoryIds.isEmpty()) {
            LambdaQueryWrapper<PetCategory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(PetCategory::getId, categoryIds);
            List<PetCategory> categories = petCategoryMapper.selectList(queryWrapper);
            
            // 转换为Map便于查找
            Map<Long, String> categoryMap = categories.stream()
                    .collect(Collectors.toMap(PetCategory::getId, PetCategory::getName));
            
            // 填充分类名称
            pets.forEach(pet -> {
                if (pet.getCategoryId() != null) {
                    pet.setCategoryName(categoryMap.get(pet.getCategoryId()));

                }
            });
        }
    }
    
    /**
     * 获取宠物详情
     */
    public Pet getPetById(Long id) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            throw new ServiceException("宠物不存在");
        }
        
        // 填充分类名称
        if (pet.getCategoryId() != null) {
            try {
                PetCategory category = petCategoryService.getCategoryById(pet.getCategoryId());
                pet.setCategoryName(category.getName());

            } catch (Exception e) {
                // 如果分类不存在，忽略异常
            }
        }
        
        return pet;
    }
    
    /**
     * 添加宠物
     */
    public void addPet(Pet pet) {
        // 设置初始领养状态为可领养
        pet.setAdoptionStatus(AdoptionStatus.AVAILABLE.getValue());
        

        
        petMapper.insert(pet);
    }
    
    /**
     * 更新宠物信息
     */
    public void updatePet(Pet pet) {
        Pet dbPet = getPetById(pet.getId());

        
        petMapper.updateById(pet);
    }
    
    /**
     * 删除宠物
     */
    public void deletePet(Long id) {
        Pet pet = getPetById(id);
        petMapper.deleteById(id);
    }
    
    /**
     * 获取宠物列表（带用户申请状态）
     */
    public List<PetWithAdoptionStatusDTO> getPetsWithUserAdoptionStatus(Long userId) {
        // 获取所有宠物
        List<Pet> pets = petMapper.selectList(null);
        if (pets.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 填充分类名称
        fillCategoryNames(pets);
        
        // 获取用户所有申请记录
        LambdaQueryWrapper<Adoption> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Adoption::getUserId, userId);
        List<Adoption> adoptions = adoptionMapper.selectList(queryWrapper);
        
        // 封装结果
        return pets.stream().map(pet -> {
            PetWithAdoptionStatusDTO dto = new PetWithAdoptionStatusDTO();
            BeanUtils.copyProperties(pet, dto);
            
            // 查找用户针对该宠物的申请
            Adoption adoption = adoptions.stream()
                    .filter(a -> a.getPetId().equals(pet.getId()))
                    .findFirst()
                    .orElse(null);
            
            if (adoption != null) {
                dto.setUserApplicationStatus(adoption.getStatus());
                dto.setAdoptionId(adoption.getId());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取宠物详情（带用户申请状态）
     */
    public PetWithAdoptionStatusDTO getPetWithUserAdoptionStatus(Long petId, Long userId) {
        // 获取宠物信息
        Pet pet = getPetById(petId);
        
        PetWithAdoptionStatusDTO dto = new PetWithAdoptionStatusDTO();
        BeanUtils.copyProperties(pet, dto);
        
        // 查询用户针对此宠物的申请记录
        LambdaQueryWrapper<Adoption> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Adoption::getPetId, petId);
        queryWrapper.eq(Adoption::getUserId, userId);
        Adoption adoption = adoptionMapper.selectOne(queryWrapper);
        
        if (adoption != null) {
            dto.setUserApplicationStatus(adoption.getStatus());
            dto.setAdoptionId(adoption.getId());
        }
        
        return dto;
    }
    
    /**
     * 更新宠物领养状态
     */
    public void updatePetAdoptionStatus(Long petId, String status) {
        Pet pet = getPetById(petId);
        
        // 只允许设置为可领养或已领养
        if (AdoptionStatus.AVAILABLE.getValue().equals(status) || 
            AdoptionStatus.ADOPTED.getValue().equals(status)) {
            pet.setAdoptionStatus(status);
            petMapper.updateById(pet);
        } else {
            throw new ServiceException("无效的领养状态");
        }
    }
    
    /**
     * 根据分类ID查询宠物数量
     */
    public long countByCategoryId(Long categoryId) {
        LambdaQueryWrapper<Pet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Pet::getCategoryId, categoryId);
        return petMapper.selectCount(queryWrapper);
    }
    
    /**
     * 获取推荐宠物列表
     */
    public List<Pet> getRecommendPets(Integer limit) {
        LambdaQueryWrapper<Pet> queryWrapper = new LambdaQueryWrapper<>();
        // 只查询可领养的宠物
        queryWrapper.eq(Pet::getAdoptionStatus, AdoptionStatus.AVAILABLE.getValue());
        // 按创建时间倒序，获取最新的宠物
        queryWrapper.orderByDesc(Pet::getCreateTime);
        // 限制返回数量
        queryWrapper.last("LIMIT " + limit);
        
        List<Pet> pets = petMapper.selectList(queryWrapper);
        // 填充分类名称
        fillCategoryNames(pets);
        return pets;
    }
    
    /**
     * 搜索宠物
     * @param keyword 关键词
     * @param currentPage 当前页
     * @param size 每页大小
     * @return 分页结果
     */
    public Page<Pet> searchPets(String keyword, Integer currentPage, Integer size) {
        if (StringUtils.isBlank(keyword)) {
            return new Page<>(currentPage, size);
        }
        
        LambdaQueryWrapper<Pet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Pet::getName, keyword)
                .or()
                .like(Pet::getBreed, keyword)
                .or()
                .like(Pet::getDescription, keyword)
                .or()
                .like(Pet::getHealthStatus, keyword);
        
        // 只搜索可领养的宠物
        queryWrapper.eq(Pet::getAdoptionStatus, AdoptionStatus.AVAILABLE.getValue());
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Pet::getCreateTime);
        
        Page<Pet> pageResult = petMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        
        // 填充分类名称
        fillCategoryNames(pageResult.getRecords());
        
        return pageResult;
    }
} 