package org.example.springboot.enumClass;

import lombok.Getter;

@Getter
public enum ReturnStatusEnum {
    PENDING("待处理"),
    APPROVED("已同意"),
    REJECTED("已拒绝");
    
    private final String value;
    
    ReturnStatusEnum(String value) {
        this.value = value;
    }
    
    public static ReturnStatusEnum fromValue(String value) {
        for (ReturnStatusEnum status : ReturnStatusEnum.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid return status: " + value);
    }
} 