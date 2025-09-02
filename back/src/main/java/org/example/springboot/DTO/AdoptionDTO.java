package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "领养申请DTO")
public class AdoptionDTO {
    
    @Schema(description = "宠物ID")
    private Long petId;
    
    @Schema(description = "申请理由")
    private String applyReason;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "地址")
    private String address;
} 