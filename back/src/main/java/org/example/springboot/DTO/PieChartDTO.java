package org.example.springboot.DTO;

import lombok.Data;

/**
 * 饼图数据传输对象
 */
@Data
public class PieChartDTO {
    /**
     * 名称
     */
    private String name;
    
    /**
     * 数值
     */
    private Integer value;
} 