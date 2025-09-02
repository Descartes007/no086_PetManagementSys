package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.example.springboot.DTO.CartDTO;
import org.example.springboot.common.Result;
import org.example.springboot.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 购物车控制器
 */
@Tag(name = "购物车接口")
@RestController
@RequestMapping("/cart")
public class CartController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CartController.class);
    
    @Resource
    private CartService cartService;
    
    @Operation(summary = "添加商品到购物车")
    @PostMapping("/add")
    public Result<?> addToCart(
            HttpSession session,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        CartDTO cartDTO = cartService.addToCart(session, productId, quantity);
        return Result.success(cartDTO);
    }
    
    @Operation(summary = "更新购物车商品数量")
    @PutMapping("/update")
    public Result<?> updateCartItemQuantity(
            HttpSession session,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        CartDTO cartDTO = cartService.updateCartItemQuantity(session, productId, quantity);
        return Result.success(cartDTO);
    }
    
    @Operation(summary = "从购物车移除商品")
    @DeleteMapping("/remove")
    public Result<?> removeFromCart(
            HttpSession session,
            @RequestParam Long productId) {
        CartDTO cartDTO = cartService.removeFromCart(session, productId);
        return Result.success(cartDTO);
    }
    
    @Operation(summary = "清空购物车")
    @DeleteMapping("/clear")
    public Result<?> clearCart(HttpSession session) {
        CartDTO cartDTO = cartService.clearCart(session);
        return Result.success(cartDTO);
    }
    
    @Operation(summary = "获取购物车")
    @GetMapping
    public Result<?> getCart(HttpSession session) {
        CartDTO cartDTO = cartService.getCart(session);
        return Result.success(cartDTO);
    }
} 