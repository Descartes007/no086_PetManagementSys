package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.springboot.entity.PetHealthRecord;

/**
 * 宠物健康记录DTO，扩展PetHealthRecord
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "宠物健康记录DTO")
public class PetHealthRecordDTO extends PetHealthRecord {
    
    @Schema(description = "宠物名称")
    private String petName;
    
    @Schema(description = "用户名称")
    private String userName;
    
    // 扩展可能需要的其他展示字段
} 