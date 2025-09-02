package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Shipping;
import org.example.springboot.service.ShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 发货信息控制器
 */
@Tag(name = "发货管理接口")
@RestController
@RequestMapping("/shipping")
public class ShippingController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ShippingController.class);
    
    @Resource
    private ShippingService shippingService;
    
    @Operation(summary = "创建发货信息")
    @PostMapping
    public Result<?> createShipping(@RequestBody Shipping shipping) {
        boolean success = shippingService.createShipping(shipping);
        if (success) {
            return Result.success("发货成功");
        } else {
            return Result.error("发货失败");
        }
    }
    
    @Operation(summary = "根据ID获取发货信息")
    @GetMapping("/{id}")
    public Result<?> getShippingById(@PathVariable Long id) {
        Shipping shipping = shippingService.getShippingById(id);
        return Result.success(shipping);
    }
    
    @Operation(summary = "根据订单ID获取发货信息")
    @GetMapping("/order/{orderId}")
    public Result<?> getShippingByOrderId(@PathVariable Long orderId) {
        Shipping shipping = shippingService.getShippingByOrderId(orderId);
        return Result.success(shipping);
    }
    
    @Operation(summary = "根据订单号获取发货信息")
    @GetMapping("/orderNo/{orderNo}")
    public Result<?> getShippingByOrderNo(@PathVariable String orderNo) {
        List<Shipping> shippings = shippingService.getShippingByOrderNo(orderNo);
        return Result.success(shippings);
    }
    
    @Operation(summary = "分页查询发货信息")
    @GetMapping("/page")
    public Result<?> getShippingsByPage(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String shippingStatus,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Shipping> page = shippingService.getShippingsByPage(orderNo, shippingStatus, currentPage, size);
        return Result.success(page);
    }
    
    @Operation(summary = "更新发货信息")
    @PutMapping("/{id}")
    public Result<?> updateShipping(@PathVariable Long id, @RequestBody Shipping shipping) {
        try {
            LOGGER.info("接收到更新物流信息请求，ID：{}，状态：{}", id, shipping.getShippingStatus());
            if (shipping.getReceiptTime() != null) {
                LOGGER.info("接收时间：{}", shipping.getReceiptTime());
            }
            
            shipping.setId(id);
            boolean success = shippingService.updateShipping(shipping);
            if (success) {
                return Result.success("更新成功");
            } else {
                LOGGER.error("更新物流信息失败，ID：{}", id);
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            LOGGER.error("更新物流信息异常", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    
    @Operation(summary = "确认收货")
    @PutMapping("/confirm")
    public Result<?> confirmReceipt(@RequestParam Long orderId, @RequestAttribute("userId") Long userId) {
        boolean success = shippingService.confirmReceipt(orderId, userId);
        if (success) {
            return Result.success("确认收货成功");
        } else {
            return Result.error("确认收货失败");
        }
    }
} 