package org.example.springboot.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.springboot.DTO.PetHealthRecordDTO;
import org.example.springboot.entity.PetHealthRecord;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.PetHealthRecordMapper;
import org.example.springboot.mapper.PetMapper;
import org.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 宠物健康记录服务类
 */
@Service
public class PetHealthRecordService {
    
    @Resource
    private PetHealthRecordMapper petHealthRecordMapper;
    
    @Resource
    private PetMapper petMapper;
    
    @Resource
    private UserMapper userMapper;
    
    /**
     * 添加健康记录
     */
    public PetHealthRecord addHealthRecord(PetHealthRecord record) {
        // 检查宠物是否存在
        if (petMapper.selectById(record.getPetId()) == null) {
            throw new ServiceException("宠物不存在");
        }
        
        // 检查用户是否存在
        if (userMapper.selectById(record.getUserId()) == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 设置记录时间
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDateTime.now());
        }
        
        petHealthRecordMapper.insert(record);
        return record;
    }
    
    /**
     * 更新健康记录
     */
    public PetHealthRecord updateHealthRecord(PetHealthRecord record) {
        if (record.getId() == null) {
            throw new ServiceException("记录ID不能为空");
        }
        
        PetHealthRecord existRecord = petHealthRecordMapper.selectById(record.getId());
        if (existRecord == null) {
            throw new ServiceException("记录不存在");
        }
        
        petHealthRecordMapper.updateById(record);
        return record;
    }
    
    /**
     * 删除健康记录
     */
    public void deleteHealthRecord(Long id) {
        PetHealthRecord record = petHealthRecordMapper.selectById(id);
        if (record == null) {
            throw new ServiceException("记录不存在");
        }
        
        petHealthRecordMapper.deleteById(id);
    }
    
    /**
     * 根据ID获取健康记录
     */
    public PetHealthRecord getHealthRecordById(Long id) {
        PetHealthRecord record = petHealthRecordMapper.selectById(id);
        if (record == null) {
            throw new ServiceException("记录不存在");
        }
        return record;
    }
    
    /**
     * 分页查询健康记录
     */
    public Page<PetHealthRecordDTO> getHealthRecords(Long petId, String recordType, int currentPage, int size) {
        Page<PetHealthRecordDTO> page = new Page<>(currentPage, size);
        return petHealthRecordMapper.selectHealthRecordDtoPage(page, petId, recordType);
    }
} 