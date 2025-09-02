package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.service.PetService;
import org.example.springboot.service.ServiceService;
import org.example.springboot.service.TrainingCourseService;
import org.example.springboot.DTO.SearchResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 搜索功能接口
 */
@Tag(name = "搜索接口")
@RestController
@RequestMapping("/search")
@CrossOrigin
public class SearchController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchController.class);

    @Resource
    private PetService petService;

    @Resource
    private ServiceService serviceService;

    @Resource
    private TrainingCourseService trainingCourseService;

    @Operation(summary = "综合搜索接口")
    @GetMapping
    public Result<?> search(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "true") Boolean searchPets,
            @RequestParam(required = false, defaultValue = "true") Boolean searchServices,
            @RequestParam(required = false, defaultValue = "true") Boolean searchTrainingCourses,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        LOGGER.info("执行综合搜索: keyword={}, searchPets={}, searchServices={}, searchTrainingCourses={}, currentPage={}, size={}",
                keyword, searchPets, searchServices, searchTrainingCourses, currentPage, size);
        
        SearchResultDTO result = new SearchResultDTO();
        
        // 根据选择的搜索范围查询相应内容
        if (searchPets) {
            result.setPets(petService.searchPets(keyword, currentPage, size));
        }
        
        if (searchServices) {
            result.setServices(serviceService.searchServices(keyword, currentPage, size));
        }
        
        if (searchTrainingCourses) {
            result.setTrainingCourses(trainingCourseService.searchCourses(keyword, currentPage, size));
        }
        
        return Result.success(result);
    }
} 