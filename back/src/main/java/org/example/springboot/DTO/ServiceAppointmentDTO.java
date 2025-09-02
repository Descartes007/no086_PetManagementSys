package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务预约数据传输对象
 */
@Data
@Schema(description = "服务预约DTO")
public class ServiceAppointmentDTO {
    
    @Schema(description = "预约ID")
    private Long id;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "服务ID")
    private Long serviceId;
    
    @Schema(description = "服务名称")
    private String serviceName;
    
    @Schema(description = "预约时间")
    private LocalDateTime appointmentTime;
    
    @Schema(description = "状态")
    private String status;
    
    @Schema(description = "宠物名称")
    private String petName;
    
    @Schema(description = "特殊要求")
    private String requirements;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "用户名称")
    private String userName;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "状态更新备注")
    private String remark;
    
    @Schema(description = "操作人ID")
    private Long operatorId;
    
    @Schema(description = "操作人姓名")
    private String operatorName;
} 