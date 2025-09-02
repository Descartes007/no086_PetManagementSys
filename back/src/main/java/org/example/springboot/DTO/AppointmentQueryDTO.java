package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 预约查询参数DTO
 */
@Data
@Schema(description = "预约查询参数")
public class AppointmentQueryDTO {
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "服务名称")
    private String serviceName;
    
    @Schema(description = "宠物名称")
    private String petName;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "预约状态")
    private String status;
    
    @Schema(description = "开始日期")
    private String startDate;
    
    @Schema(description = "结束日期")
    private String endDate;
    
    @Schema(description = "当前页码")
    private Integer currentPage = 1;
    
    @Schema(description = "每页大小")
    private Integer size = 10;
} 