package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 宠物领养状态枚举
 */
@Getter
public enum AdoptionStatus {
    
    AVAILABLE("可领养"),
    ADOPTED("已领养");
    
    private final String value;
    
    AdoptionStatus(String value) {
        this.value = value;
    }
} 