package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Order;
import org.example.springboot.entity.OrderReview;
import org.example.springboot.entity.User;
import org.example.springboot.enumClass.OrderStatusEnum;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.OrderMapper;
import org.example.springboot.mapper.OrderReviewMapper;
import org.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderReviewService {
    
    @Resource
    private OrderReviewMapper orderReviewMapper;
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private UserMapper userMapper;
    
    /**
     * 创建评价
     */
    @Transactional
    public OrderReview createReview(OrderReview review) {
        // 检查订单是否存在
        Order order = orderMapper.selectById(review.getOrderId());
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 检查订单状态是否为已完成
        if (!OrderStatusEnum.COMPLETED.getValue().equals(order.getStatus())) {
            throw new ServiceException("只能评价已完成的订单");
        }
        
        // 检查是否已经评价过
        if (order.getIsReviewed()) {
            throw new ServiceException("该订单已评价");
        }
        
        // 设置评价信息
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        
        // 保存评价
        orderReviewMapper.insert(review);
        
        // 更新订单评价状态
        order.setIsReviewed(true);
        order.setReviewTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        return review;
    }
    
    /**
     * 商家回复评价
     */
    @Transactional
    public boolean replyReview(Long reviewId, String reply) {
        OrderReview review = orderReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new ServiceException("评价不存在");
        }
        
        review.setReply(reply);
        review.setReplyTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        
        return orderReviewMapper.updateById(review) > 0;
    }
    
    /**
     * 获取评价详情
     */
    public OrderReview getReviewById(Long id) {
        OrderReview review = orderReviewMapper.selectById(id);
        if (review != null) {
            enrichReviewWithUserInfo(review);
        }
        return review;
    }
    
    /**
     * 获取订单的评价
     */
    public OrderReview getReviewByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderReview::getOrderId, orderId);
        OrderReview review = orderReviewMapper.selectOne(queryWrapper);
        if (review != null) {
            enrichReviewWithUserInfo(review);
        }
        return review;
    }
    
    /**
     * 获取商品的评价列表
     */
    public List<OrderReview> getProductReviews(Long productId) {
        LambdaQueryWrapper<OrderReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderReview::getProductId, productId);
        queryWrapper.orderByDesc(OrderReview::getCreateTime);
        List<OrderReview> reviews = orderReviewMapper.selectList(queryWrapper);
        
        // 填充用户信息
        return reviews.stream()
            .map(this::enrichReviewWithUserInfo)
            .collect(Collectors.toList());
    }
    
    /**
     * 分页查询评价
     */
    public Page<OrderReview> getReviewsByPage(Long userId, Long productId, Integer currentPage, Integer size) {
        LambdaQueryWrapper<OrderReview> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(OrderReview::getUserId, userId);
        }
        if (productId != null) {
            queryWrapper.eq(OrderReview::getProductId, productId);
        }
        queryWrapper.orderByDesc(OrderReview::getCreateTime);
        
        Page<OrderReview> page = orderReviewMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        
        // 填充用户信息
        page.getRecords().forEach(this::enrichReviewWithUserInfo);
        
        return page;
    }
    
    /**
     * 填充评价的用户信息
     */
    private OrderReview enrichReviewWithUserInfo(OrderReview review) {
        if (review != null && review.getUserId() != null && !review.getIsAnonymous()) {
            User user = userMapper.selectById(review.getUserId());
            if (user != null) {
                // 清除敏感信息
                user.setPassword(null);
                user.setToken(null);
                user.setMenuList(null);
                review.setUser(user);
            }
        }
        return review;
    }
} 