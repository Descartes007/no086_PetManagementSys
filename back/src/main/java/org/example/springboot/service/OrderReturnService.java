package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.entity.Order;
import org.example.springboot.entity.OrderReturn;
import org.example.springboot.entity.Shipping;
import org.example.springboot.enumClass.OrderStatusEnum;
import org.example.springboot.enumClass.ReturnStatusEnum;
import org.example.springboot.enumClass.ShippingStatusEnum;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.OrderMapper;
import org.example.springboot.mapper.OrderReturnMapper;
import org.example.springboot.mapper.ShippingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderReturnService {
    
    @Resource
    private OrderReturnMapper orderReturnMapper;
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private ShippingMapper shippingMapper;
    
    /**
     * 申请退货
     */
    @Transactional
    public OrderReturn applyReturn(OrderReturn orderReturn) {
        // 检查订单是否存在
        Order order = orderMapper.selectById(orderReturn.getOrderId());
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 检查订单状态是否允许退货
        if (!OrderStatusEnum.COMPLETED.getValue().equals(order.getStatus()) 
                && !OrderStatusEnum.PENDING_RECEIPT.getValue().equals(order.getStatus())) {
            throw new ServiceException("当前订单状态不允许退货");
        }
        
        // 检查是否已经申请过退货
        if (order.getIsReturned()) {
            throw new ServiceException("该订单已申请过退货");
        }
        
        // 设置退货信息
        orderReturn.setStatus(ReturnStatusEnum.PENDING.getValue());
        orderReturn.setCreateTime(LocalDateTime.now());
        orderReturn.setUpdateTime(LocalDateTime.now());
        // 记录退货前的订单状态
        orderReturn.setPreviousOrderStatus(order.getStatus());
        
        // 保存退货申请
        orderReturnMapper.insert(orderReturn);
        
        // 更新订单状态
        order.setStatus(OrderStatusEnum.RETURNING.getValue());
        order.setIsReturned(true);
        order.setReturnTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        return orderReturn;
    }
    
    /**
     * 处理退货申请
     */
    @Transactional
    public boolean processReturn(Long returnId, String status, String operatorNote, String operator) {
        OrderReturn orderReturn = orderReturnMapper.selectById(returnId);
        if (orderReturn == null) {
            throw new ServiceException("退货申请不存在");
        }
        
        // 只能处理待处理状态的退货申请
        if (!ReturnStatusEnum.PENDING.getValue().equals(orderReturn.getStatus())) {
            throw new ServiceException("该退货申请已处理");
        }
        
        // 更新退货申请状态
        orderReturn.setStatus(status);
        orderReturn.setOperatorNote(operatorNote);
        orderReturn.setOperator(operator);
        orderReturn.setUpdateTime(LocalDateTime.now());
        orderReturn.setProcessTime(LocalDateTime.now());
        
        // 更新订单状态
        Order order = orderMapper.selectById(orderReturn.getOrderId());
        if (ReturnStatusEnum.APPROVED.getValue().equals(status)) {
            order.setStatus(OrderStatusEnum.RETURNED.getValue());
            
            // 更新物流状态为"已退回"
            LambdaQueryWrapper<Shipping> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Shipping::getOrderId, order.getId());
            Shipping shipping = shippingMapper.selectOne(queryWrapper);
            if (shipping != null) {
                shipping.setShippingStatus(ShippingStatusEnum.RETURNED.getValue());
                shipping.setUpdateTime(LocalDateTime.now());
                shippingMapper.updateById(shipping);
            }
        } else if (ReturnStatusEnum.REJECTED.getValue().equals(status)) {
            // 恢复到退货前的状态
            order.setStatus(orderReturn.getPreviousOrderStatus() != null ? 
                orderReturn.getPreviousOrderStatus() : OrderStatusEnum.COMPLETED.getValue());
            // 不要将isReturned设为false，因为需要保留退货记录
            // order.setIsReturned(false);
            // 不要清除退货时间，因为需要记录退货申请的时间
            // order.setReturnTime(null);
        }
        order.setUpdateTime(LocalDateTime.now());
        
        return orderReturnMapper.updateById(orderReturn) > 0 
                && orderMapper.updateById(order) > 0;
    }
    
    /**
     * 获取退货申请详情
     */
    public OrderReturn getReturnById(Long id) {
        return orderReturnMapper.selectById(id);
    }
    
    /**
     * 根据订单ID获取退货信息
     */
    public OrderReturn getReturnByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderReturn> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderReturn::getOrderId, orderId);
        return orderReturnMapper.selectOne(queryWrapper);
    }
    
    /**
     * 分页查询退货申请
     */
    public Page<OrderReturn> getReturnsByPage(Long userId, String status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<OrderReturn> queryWrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq(OrderReturn::getUserId, userId);
        }
        if (status != null) {
            queryWrapper.eq(OrderReturn::getStatus, status);
        }
        queryWrapper.orderByDesc(OrderReturn::getCreateTime);
        
        return orderReturnMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }
} 