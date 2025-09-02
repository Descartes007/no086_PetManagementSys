package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 购物车项数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "购物车项")
public class CartItemDTO {
    
    @Schema(description = "商品ID")
    private Long productId;
    
    @Schema(description = "商品名称")
    private String productName;
    
    @Schema(description = "商品图片")
    private String productImage;
    
    @Schema(description = "商品价格")
    private BigDecimal price;
    
    @Schema(description = "商品数量")
    private Integer quantity;
    
    @Schema(description = "商品总金额")
    private BigDecimal totalAmount;
} 