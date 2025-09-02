package org.example.springboot.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建订单数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建订单请求")
public class OrderCreateDTO {
    
    @Schema(description = "购物车商品列表")
    private List<CartItemDTO> items;
    
    @Schema(description = "收货地址")
    private String address;
    
    @Schema(description = "联系人")
    private String contactName;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "订单备注")
    private String remark;
    
    @Schema(description = "支付方式")
    private String paymentMethod;
} 