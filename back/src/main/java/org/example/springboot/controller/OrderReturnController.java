package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.OrderReturn;
import org.example.springboot.service.OrderReturnService;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单退货接口")
@RestController
@RequestMapping("/order/return")
public class OrderReturnController {
    
    @Resource
    private OrderReturnService orderReturnService;
    
    @Operation(summary = "申请退货")
    @PostMapping
    public Result<?> applyReturn(@RequestBody OrderReturn orderReturn) {
        OrderReturn result = orderReturnService.applyReturn(orderReturn);
        return Result.success("退货申请提交成功", result);
    }
    
    @Operation(summary = "处理退货申请")
    @PutMapping("/{id}/process")
    public Result<?> processReturn(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String operatorNote,
            @RequestParam String operator) {
        boolean success = orderReturnService.processReturn(id, status, operatorNote, operator);
        return success ? Result.success("处理成功") : Result.error("处理失败");
    }
    
    @Operation(summary = "获取退货申请详情")
    @GetMapping("/{id}")
    public Result<?> getReturnById(@PathVariable Long id) {
        OrderReturn orderReturn = orderReturnService.getReturnById(id);
        return Result.success(orderReturn);
    }
    
    @Operation(summary = "根据订单ID获取退货信息")
    @GetMapping("/order/{orderId}")
    public Result<?> getReturnByOrderId(@PathVariable Long orderId) {
        OrderReturn orderReturn = orderReturnService.getReturnByOrderId(orderId);
        return Result.success(orderReturn);
    }
    
    @Operation(summary = "分页查询退货申请")
    @GetMapping("/page")
    public Result<?> getReturnsByPage(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<OrderReturn> page = orderReturnService.getReturnsByPage(userId, status, currentPage, size);
        return Result.success(page);
    }
} 