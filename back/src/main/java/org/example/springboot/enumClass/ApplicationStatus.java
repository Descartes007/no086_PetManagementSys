package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 领养申请状态枚举
 */
@Getter
public enum ApplicationStatus {
    
    APPLIED("已申请"),
    REVIEWING("审核中"),
    APPROVED("已通过"),
    REJECTED("已拒绝");
    
    private final String value;
    
    ApplicationStatus(String value) {
        this.value = value;
    }
} 