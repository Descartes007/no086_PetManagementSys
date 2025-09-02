package org.example.springboot.DTO;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.example.springboot.entity.Pet;
import org.example.springboot.entity.Service;
import org.example.springboot.entity.TrainingCourse;

/**
 * 搜索结果DTO，包含宠物、服务、训练课程的搜索结果
 */
@Data
public class SearchResultDTO {
    
    /**
     * 宠物搜索结果
     */
    private Page<Pet> pets;
    
    /**
     * 服务搜索结果
     */
    private Page<Service> services;
    
    /**
     * 训练课程搜索结果
     */
    private Page<TrainingCourse> trainingCourses;
} 