package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.example.springboot.entity.Pet;

@Data
@Schema(description = "包含领养状态的宠物信息")
public class PetWithAdoptionStatusDTO extends Pet {
    
    @Schema(description = "用户申请状态")
    private String userApplicationStatus;
    
    @Schema(description = "申请ID")
    private Long adoptionId;
} 