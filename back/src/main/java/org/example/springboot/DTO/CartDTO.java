package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "购物车")
public class CartDTO {
    
    @Schema(description = "购物车项列表")
    private List<CartItemDTO> items;
    
    @Schema(description = "商品总数量")
    private Integer totalQuantity;
    
    @Schema(description = "商品总金额")
    private BigDecimal totalAmount;
} 