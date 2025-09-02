package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.springboot.entity.PetVaccination;

/**
 * 宠物疫苗接种记录DTO，扩展PetVaccination
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "宠物疫苗接种记录DTO")
public class PetVaccinationDTO extends PetVaccination {
    
    @Schema(description = "宠物名称")
    private String petName;
    
    // 扩展可能需要的其他展示字段
} 