package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "训练查询DTO")
public class TrainingQueryDTO {
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "课程名称")
    private String courseName;
    
    @Schema(description = "课程分类")
    private String category;
    
    @Schema(description = "宠物名称")
    private String petName;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "状态")
    private String status;
    
    @Schema(description = "开始日期")
    private String startDate;
    
    @Schema(description = "结束日期")
    private String endDate;
    
    @Schema(description = "当前页")
    private Integer currentPage = 1;
    
    @Schema(description = "每页数量")
    private Integer size = 10;
} 