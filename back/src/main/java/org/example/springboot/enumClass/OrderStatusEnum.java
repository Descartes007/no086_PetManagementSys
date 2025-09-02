package org.example.springboot.enumClass;

import lombok.Getter;

/**
 * 订单状态枚举类
 */
@Getter
public enum OrderStatusEnum {
    
    PENDING_PAYMENT("待付款"),
    PENDING_DELIVERY("待发货"),
    PENDING_RECEIPT("待收货"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    RETURNING("退货中"),
    RETURNED("已退货");
    
    private final String value;
    
    OrderStatusEnum(String value) {
        this.value = value;
    }
    
    public static OrderStatusEnum fromValue(String value) {
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid order status: " + value);
    }
} 