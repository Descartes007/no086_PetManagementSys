package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.entity.ServiceCategory;
import org.example.springboot.entity.TrainingCourse;
import org.example.springboot.entity.TrainingCategory;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ServiceCategoryMapper;
import org.example.springboot.mapper.TrainingCourseMapper;
import org.example.springboot.mapper.TrainingCategoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 训练课程服务类
 */
@Service
public class TrainingCourseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingCourseService.class);

    @Resource
    private TrainingCourseMapper trainingCourseMapper;

    @Resource
    private TrainingCategoryMapper trainingCategoryMapper;
    @Autowired
    private ServiceCategoryMapper serviceCategoryMapper;

    /**
     * 创建课程
     *
     * @param course 课程信息
     * @return 课程ID
     */
    public Long createCourse(TrainingCourse course) {
        if (course == null) {
            throw new ServiceException("课程信息不能为空");
        }

        // 校验课程信息
        if (StringUtils.isBlank(course.getName())) {
            throw new ServiceException("课程名称不能为空");
        }

        if (course.getPrice() == null) {
            throw new ServiceException("课程价格不能为空");
        }

        // 验证分类是否存在
        if (course.getCategoryId() != null) {
            TrainingCategory category = trainingCategoryMapper.selectById(course.getCategoryId());
            if (category == null) {
                throw new ServiceException("所选分类不存在");
            }
            // 设置分类名称
            course.setCategory(category.getName());
        }

        // 设置默认状态为启用
        if (course.getStatus() == null) {
            course.setStatus(1);
        }

        // 设置创建时间
        course.setCreateTime(LocalDateTime.now());

        int result = trainingCourseMapper.insert(course);
        if (result <= 0) {
            throw new ServiceException("创建课程失败");
        }

        return course.getId();
    }

    /**
     * 更新课程信息
     *
     * @param course 课程信息
     * @return 是否成功
     */
    public boolean updateCourse(TrainingCourse course) {
        if (course == null || course.getId() == null) {
            throw new ServiceException("课程信息不能为空");
        }

        // 检查课程是否存在
        TrainingCourse existingCourse = trainingCourseMapper.selectById(course.getId());
        if (existingCourse == null) {
            throw new ServiceException("课程不存在");
        }

        // 验证分类是否存在，并更新分类名称
        if (course.getCategoryId() != null && !course.getCategoryId().equals(existingCourse.getCategoryId())) {
            TrainingCategory category = trainingCategoryMapper.selectById(course.getCategoryId());
            if (category == null) {
                throw new ServiceException("所选分类不存在");
            }
            // 设置分类名称
            course.setCategory(category.getName());
        }

        // 更新时间
        course.setUpdateTime(LocalDateTime.now());

        int result = trainingCourseMapper.updateById(course);
        return result > 0;
    }

    /**
     * 获取课程详情
     *
     * @param id 课程ID
     * @return 课程详情
     */
    public TrainingCourse getCourseById(Long id) {
        if (id == null) {
            throw new ServiceException("课程ID不能为空");
        }

        TrainingCourse course = trainingCourseMapper.selectById(id);
        if (course == null) {
            throw new ServiceException("课程不存在");
        }

        return course;
    }

    /**
     * 删除课程
     *
     * @param id 课程ID
     * @return 是否成功
     */
    public boolean deleteCourse(Long id) {
        if (id == null) {
            throw new ServiceException("课程ID不能为空");
        }

        // 检查课程是否存在
        TrainingCourse course = trainingCourseMapper.selectById(id);
        if (course == null) {
            throw new ServiceException("课程不存在");
        }

        int result = trainingCourseMapper.deleteById(id);
        return result > 0;
    }

    /**
     * 分页查询课程列表
     *
     * @param name       课程名称
     * @param category   课程分类
     * @param categoryId 分类ID
     * @param page       当前页
     * @param size       每页显示条数
     * @return 分页结果
     */
    public Page<TrainingCourse> getCoursesByPage(String name, String category, Integer status, Long categoryId,
            Integer page, Integer size) {
        Page<TrainingCourse> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<TrainingCourse> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(TrainingCourse::getName, name);
        }

        if (StringUtils.isNotBlank(category)) {
            queryWrapper.eq(TrainingCourse::getCategory, category);
        }

        if (categoryId != null) {
            queryWrapper.eq(TrainingCourse::getCategoryId, categoryId);
        }
        if (status != null) {
            queryWrapper.eq(TrainingCourse::getStatus, status);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc(TrainingCourse::getCreateTime);

        Page<TrainingCourse> trainingCoursePage = trainingCourseMapper.selectPage(pageParam, queryWrapper);
        trainingCoursePage.getRecords().forEach(this::fillCategory);
        return trainingCoursePage;
    }

    private void fillCategory(TrainingCourse trainingCourse) {
        if (trainingCourse.getCategoryId() != null) {
            TrainingCategory category = trainingCategoryMapper.selectById(trainingCourse.getCategoryId());
            trainingCourse.setCategory(category.getName());
        }

    }

    /**
     * 获取所有课程分类
     *
     * @return 分类列表
     */
    public List<String> getAllCategories() {
        LambdaQueryWrapper<TrainingCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(TrainingCourse::getCategory)
                .groupBy(TrainingCourse::getCategory);

        List<TrainingCourse> courses = trainingCourseMapper.selectList(queryWrapper);
        return courses.stream()
                .map(TrainingCourse::getCategory)
                .distinct()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有启用的课程
     *
     * @return 课程列表
     */
    public List<TrainingCourse> getAllEnabledCourses() {
        LambdaQueryWrapper<TrainingCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TrainingCourse::getStatus, 1)
                .orderByDesc(TrainingCourse::getCreateTime);

        return trainingCourseMapper.selectList(queryWrapper);
    }

    /**
     * 获取指定分类的课程
     *
     * @param category 分类
     * @return 课程列表
     */
    public List<TrainingCourse> getCoursesByCategory(String category) {
        if (StringUtils.isBlank(category)) {
            throw new ServiceException("分类不能为空");
        }

        LambdaQueryWrapper<TrainingCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TrainingCourse::getCategory, category)
                .eq(TrainingCourse::getStatus, 1)
                .orderByDesc(TrainingCourse::getCreateTime);

        return trainingCourseMapper.selectList(queryWrapper);
    }

    /**
     * 更新课程状态
     *
     * @param id     课程ID
     * @param status 状态(0:停用,1:启用)
     * @return 是否成功
     */
    public boolean updateCourseStatus(Long id, Integer status) {
        if (id == null) {
            throw new ServiceException("课程ID不能为空");
        }

        if (status == null || (status != 0 && status != 1)) {
            throw new ServiceException("状态值无效");
        }

        TrainingCourse course = new TrainingCourse();
        course.setId(id);
        course.setStatus(status);
        course.setUpdateTime(LocalDateTime.now());

        int result = trainingCourseMapper.updateById(course);
        return result > 0;
    }

    /**
     * 搜索训练课程
     *
     * @param keyword     关键词
     * @param currentPage 当前页
     * @param size        每页大小
     * @return 分页结果
     */
    public Page<TrainingCourse> searchCourses(String keyword, Integer currentPage, Integer size) {
        if (StringUtils.isBlank(keyword)) {
            return new Page<>(currentPage, size);
        }

        Page<TrainingCourse> pageParam = new Page<>(currentPage, size);
        LambdaQueryWrapper<TrainingCourse> queryWrapper = new LambdaQueryWrapper<>();

        // 添加搜索条件 - 只搜索数据库字段
        queryWrapper.like(TrainingCourse::getName, keyword)
                .or()
                .like(TrainingCourse::getDescription, keyword);

        // 只搜索启用状态的课程
        queryWrapper.eq(TrainingCourse::getStatus, 1);

        // 按创建时间降序排序
        queryWrapper.orderByDesc(TrainingCourse::getCreateTime);

        Page<TrainingCourse> resultPage = trainingCourseMapper.selectPage(pageParam, queryWrapper);

        // 补充分类信息
        resultPage.getRecords().forEach(this::fillCategory);

        // 手动过滤分类名称与关键字匹配的结果 (因为category不能在数据库查询中使用)
        if (StringUtils.isNotBlank(keyword) && resultPage.getRecords().size() < size) {
            // 获取所有课程
            LambdaQueryWrapper<TrainingCourse> allCoursesQuery = new LambdaQueryWrapper<>();
            allCoursesQuery.eq(TrainingCourse::getStatus, 1);
            List<TrainingCourse> allCourses = trainingCourseMapper.selectList(allCoursesQuery);

            // 填充分类名称
            allCourses.forEach(this::fillCategory);

            // 手动过滤分类名称匹配的记录
            List<TrainingCourse> categoryMatches = allCourses.stream()
                    .filter(course -> !resultPage.getRecords().contains(course)) // 排除已经在结果中的记录
                    .filter(course -> course.getCategory() != null && course.getCategory().contains(keyword))
                    .limit(size - resultPage.getRecords().size()) // 限制结果数量
                    .collect(Collectors.toList());

            // 合并结果
            if (!categoryMatches.isEmpty()) {
                List<TrainingCourse> combinedRecords = new ArrayList<>(resultPage.getRecords());
                combinedRecords.addAll(categoryMatches);

                // 更新分页对象
                Page<TrainingCourse> newResult = new Page<>(currentPage, size,
                        resultPage.getTotal() + categoryMatches.size());
                newResult.setRecords(combinedRecords);
                return newResult;
            }
        }

        return resultPage;
    }
}