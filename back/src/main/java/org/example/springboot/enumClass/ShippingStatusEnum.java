package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 发货状态枚举类
 */
@Getter
public enum ShippingStatusEnum {
    
    PENDING_DELIVERY("待发货"),
    DELIVERED("已发货"),
    RECEIVED("已签收"),
    RETURNED("已退回");
    
    private final String value;
    
    ShippingStatusEnum(String value) {
        this.value = value;
    }
} 