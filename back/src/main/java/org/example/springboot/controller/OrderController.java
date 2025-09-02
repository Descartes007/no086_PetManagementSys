package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.DTO.OrderCreateDTO;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Order;
import org.example.springboot.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@Tag(name = "订单管理接口")
@RestController
@RequestMapping("/order")
public class OrderController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);
    
    @Resource
    private OrderService orderService;
    
    @Operation(summary = "创建订单")
    @PostMapping
    public Result<?> createOrder(@RequestParam("userId") Long userId, @RequestBody OrderCreateDTO orderCreateDTO) {
        Order order = orderService.createOrder(userId, orderCreateDTO);
        return Result.success("订单创建成功", order);
    }
    
    @Operation(summary = "根据ID获取订单")
    @GetMapping("/{id}")
    public Result<?> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return Result.success(order);
    }
    
    @Operation(summary = "获取用户订单列表")
    @GetMapping("/user")
    public Result<?> getUserOrders(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) String orderNo) {
        List<Order> orders = orderService.getOrdersByUserIdAndOrderNo(userId, orderNo);
        return Result.success(orders);
    }
    
    @Operation(summary = "分页查询订单")
    @GetMapping("/page")
    public Result<?> getOrdersByPage(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Order> page = orderService.getOrdersByPage(userId, status, currentPage, size);
        return Result.success(page);
    }
    
    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")
    public Result<?> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        boolean success = orderService.updateOrderStatus(id, status);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }
    
    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<?> cancelOrder(@PathVariable Long id, @RequestParam("userId") Long userId) {
        boolean success = orderService.cancelOrder(id, userId);
        if (success) {
            return Result.success("取消成功");
        } else {
            return Result.error("取消失败");
        }
    }
    
    @Operation(summary = "确认收货")
    @PutMapping("/{id}/confirm")
    public Result<?> confirmReceipt(@PathVariable Long id, @RequestParam("userId") Long userId) {
        boolean success = orderService.confirmReceipt(id, userId);
        if (success) {
            return Result.success("确认收货成功");
        } else {
            return Result.error("确认收货失败");
        }
    }
    
    @Operation(summary = "删除订单")
    @DeleteMapping("/{id}")
    public Result<?> deleteOrder(@PathVariable Long id) {
        boolean success = orderService.deleteOrder(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
    
    @Operation(summary = "获取订单支付剩余时间")
    @GetMapping("/{id}/payment-time")
    public Result<?> getOrderPaymentRemainingTime(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        long remainingSeconds = orderService.getRemainingPaymentTime(order);
        boolean isExpired = orderService.isOrderExpired(order);
        
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", id);
        data.put("remainingSeconds", remainingSeconds);
        data.put("isExpired", isExpired);
        data.put("paymentDeadline", order.getPaymentDeadline());
        
        return Result.success(data);
    }
} 