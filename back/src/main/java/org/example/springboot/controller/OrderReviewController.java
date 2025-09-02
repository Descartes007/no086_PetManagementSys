package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.OrderReview;
import org.example.springboot.service.OrderReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单评价接口")
@RestController
@RequestMapping("/order/review")
public class OrderReviewController {
    
    @Resource
    private OrderReviewService orderReviewService;
    
    @Operation(summary = "创建评价")
    @PostMapping
    public Result<?> createReview(@RequestBody OrderReview review) {
        OrderReview result = orderReviewService.createReview(review);
        return Result.success("评价成功", result);
    }
    
    @Operation(summary = "商家回复评价")
    @PutMapping("/{id}/reply")
    public Result<?> replyReview(@PathVariable Long id, @RequestParam String reply) {
        boolean success = orderReviewService.replyReview(id, reply);
        return success ? Result.success("回复成功") : Result.error("回复失败");
    }
    
    @Operation(summary = "获取评价详情")
    @GetMapping("/{id}")
    public Result<?> getReviewById(@PathVariable Long id) {
        OrderReview review = orderReviewService.getReviewById(id);
        return Result.success(review);
    }
    
    @Operation(summary = "获取订单的评价")
    @GetMapping("/order/{orderId}")
    public Result<?> getReviewByOrderId(@PathVariable Long orderId) {
        OrderReview review = orderReviewService.getReviewByOrderId(orderId);
        return Result.success(review);
    }
    
    @Operation(summary = "获取商品的评价列表")
    @GetMapping("/product/{productId}")
    public Result<?> getProductReviews(@PathVariable Long productId) {
        List<OrderReview> reviews = orderReviewService.getProductReviews(productId);
        return Result.success(reviews);
    }
    
    @Operation(summary = "分页查询评价")
    @GetMapping("/page")
    public Result<?> getReviewsByPage(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<OrderReview> page = orderReviewService.getReviewsByPage(userId, productId, currentPage, size);
        return Result.success(page);
    }
} 