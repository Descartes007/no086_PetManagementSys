package org.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.PetVaccinationDTO;
import org.example.springboot.entity.PetVaccination;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.PetMapper;
import org.example.springboot.mapper.PetVaccinationMapper;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

/**
 * 宠物疫苗接种记录服务类
 */
@Service
public class PetVaccinationService {
    
    @Resource
    private PetVaccinationMapper petVaccinationMapper;
    
    @Resource
    private PetMapper petMapper;
    
    /**
     * 添加疫苗接种记录
     */
    public PetVaccination addVaccination(PetVaccination vaccination) {
        // 检查宠物是否存在
        if (petMapper.selectById(vaccination.getPetId()) == null) {
            throw new ServiceException("宠物不存在");
        }
        
        // 设置接种时间
        if (vaccination.getVaccinationDate() == null) {
            vaccination.setVaccinationDate(LocalDateTime.now());
        }
        
        petVaccinationMapper.insert(vaccination);
        return vaccination;
    }
    
    /**
     * 更新疫苗接种记录
     */
    public PetVaccination updateVaccination(PetVaccination vaccination) {
        if (vaccination.getId() == null) {
            throw new ServiceException("记录ID不能为空");
        }
        
        PetVaccination existVaccination = petVaccinationMapper.selectById(vaccination.getId());
        if (existVaccination == null) {
            throw new ServiceException("记录不存在");
        }
        
        petVaccinationMapper.updateById(vaccination);
        return vaccination;
    }
    
    /**
     * 删除疫苗接种记录
     */
    public void deleteVaccination(Long id) {
        PetVaccination vaccination = petVaccinationMapper.selectById(id);
        if (vaccination == null) {
            throw new ServiceException("记录不存在");
        }
        
        petVaccinationMapper.deleteById(id);
    }
    
    /**
     * 根据ID获取疫苗接种记录
     */
    public PetVaccination getVaccinationById(Long id) {
        PetVaccination vaccination = petVaccinationMapper.selectById(id);
        if (vaccination == null) {
            throw new ServiceException("记录不存在");
        }
        return vaccination;
    }
    
    /**
     * 分页查询疫苗接种记录
     */
    public Page<PetVaccinationDTO> getVaccinations(Long petId, int currentPage, int size) {
        Page<PetVaccinationDTO> page = new Page<>(currentPage, size);
        return petVaccinationMapper.selectVaccinationDtoPage(page, petId);
    }
} 