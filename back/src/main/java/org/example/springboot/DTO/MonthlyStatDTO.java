package org.example.springboot.DTO;

import lombok.Data;

/**
 * 月度统计数据传输对象
 */
@Data
public class MonthlyStatDTO {
    /**
     * 月份 (yyyy-MM格式)
     */
    private String month;
    
    /**
     * 数量
     */
    private Integer count;
} 