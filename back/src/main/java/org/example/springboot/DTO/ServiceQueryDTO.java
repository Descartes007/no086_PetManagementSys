package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 服务查询参数DTO
 */
@Data
@Schema(description = "服务查询参数")
public class ServiceQueryDTO {
    
    @Schema(description = "服务名称")
    private String name;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "状态(0:停用,1:启用)")
    private Integer status;
    
    @Schema(description = "当前页码")
    private Integer currentPage = 1;
    
    @Schema(description = "每页大小")
    private Integer size = 10;
} 