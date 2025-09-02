package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "训练预约DTO")
public class TrainingAppointmentDTO {
    
    @Schema(description = "预约ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名称")
    private String userName;
    
    @Schema(description = "课程ID")
    private Long courseId;
    
    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程价格")
    private BigDecimal coursePrice;
    
    @Schema(description = "课程分类")
    private String category;
    
    @Schema(description = "宠物名称")
    private String petName;
    
    @Schema(description = "预约时间")
    private LocalDateTime appointmentTime;
    
    @Schema(description = "状态")
    private String status;
    
    @Schema(description = "特殊要求")
    private String requirements;
    
    @Schema(description = "进度")
    private Integer progress;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "操作人ID")
    private Long operatorId;
    
    @Schema(description = "操作人姓名")
    private String operatorName;
    
    @Schema(description = "备注")
    private String remark;
    
    @Schema(description = "评分")
    private Integer rating;
    
    @Schema(description = "反馈")
    private String feedback;
    
    @Schema(description = "反馈时间")
    private LocalDateTime feedbackTime;
    
    @Schema(description = "是否有反馈")
    private Boolean hasFeedback;
} 