package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.entity.Order;
import org.example.springboot.entity.Shipping;
import org.example.springboot.enumClass.OrderStatusEnum;
import org.example.springboot.enumClass.ShippingStatusEnum;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.ShippingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 发货信息服务实现类
 */
@Service
public class ShippingService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ShippingService.class);
    
    @Resource
    private ShippingMapper shippingMapper;
    
    @Resource
    private OrderService orderService;
    
    /**
     * 创建发货信息
     * @param shipping 发货信息对象
     * @return 是否成功
     */
    @Transactional
    public boolean createShipping(Shipping shipping) {
        // 检查订单是否存在
        Order order = orderService.getOrderById(shipping.getOrderId());
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 检查订单状态是否为待发货
        if (!OrderStatusEnum.PENDING_DELIVERY.getValue().equals(order.getStatus())) {
            throw new ServiceException("订单状态不是待发货");
        }
        
        // 检查是否已经存在发货信息
        LambdaQueryWrapper<Shipping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Shipping::getOrderId, shipping.getOrderId());
        if (shippingMapper.selectCount(queryWrapper) > 0) {
            throw new ServiceException("发货信息已存在");
        }
        
        // 生成发货单号
        if (StringUtils.isBlank(shipping.getShippingNo())) {
            shipping.setShippingNo(generateShippingNo());
        }
        
        // 设置订单号
        shipping.setOrderNo(order.getOrderNo());
        
        // 设置发货状态
        if (StringUtils.isBlank(shipping.getShippingStatus())) {
            shipping.setShippingStatus(ShippingStatusEnum.DELIVERED.getValue());
        }
        
        // 设置发货时间
        shipping.setDeliveryTime(LocalDateTime.now());
        shipping.setCreateTime(LocalDateTime.now());
        shipping.setUpdateTime(LocalDateTime.now());
        
        boolean result = shippingMapper.insert(shipping) > 0;
        
        // 更新订单状态为待收货
        if (result) {
            orderService.updateOrderStatus(order.getId(), OrderStatusEnum.PENDING_RECEIPT.getValue());
        }
        
        return result;
    }
    
    /**
     * 根据ID获取发货信息
     * @param id 发货信息ID
     * @return 发货信息对象
     */
    public Shipping getShippingById(Long id) {
        Shipping shipping = shippingMapper.selectById(id);
        if (shipping == null) {
            throw new ServiceException("发货信息不存在");
        }
        return shipping;
    }
    
    /**
     * 根据订单ID获取发货信息
     * @param orderId 订单ID
     * @return 发货信息对象
     */
    public Shipping getShippingByOrderId(Long orderId) {
        LambdaQueryWrapper<Shipping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Shipping::getOrderId, orderId);
        queryWrapper.last("LIMIT 1");
        return shippingMapper.selectOne(queryWrapper);
    }
    
    /**
     * 根据订单号获取发货信息
     * @param orderNo 订单号
     * @return 发货信息列表
     */
    public List<Shipping> getShippingByOrderNo(String orderNo) {
        LambdaQueryWrapper<Shipping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Shipping::getOrderNo, orderNo);
        queryWrapper.orderByDesc(Shipping::getCreateTime);
        return shippingMapper.selectList(queryWrapper);
    }
    
    /**
     * 分页查询发货信息
     * @param orderNo 订单号
     * @param shippingStatus 发货状态
     * @param currentPage 当前页
     * @param size 每页大小
     * @return 分页发货信息列表
     */
    public Page<Shipping> getShippingsByPage(String orderNo, String shippingStatus, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Shipping> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(orderNo), Shipping::getOrderNo, orderNo);
        queryWrapper.eq(StringUtils.isNotBlank(shippingStatus), Shipping::getShippingStatus, shippingStatus);
        queryWrapper.orderByDesc(Shipping::getCreateTime);
        
        return shippingMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }
    
    /**
     * 更新发货信息
     * @param shipping 发货信息对象
     * @return 是否成功
     */
    @Transactional
    public boolean updateShipping(Shipping shipping) {
        // 检查发货信息是否存在
        Shipping existingShipping = getShippingById(shipping.getId());
        if (existingShipping == null) {
            throw new ServiceException("发货信息不存在");
        }
        
        shipping.setUpdateTime(LocalDateTime.now());
        
        // 如果状态变更为已签收，更新签收时间
        if (ShippingStatusEnum.RECEIVED.getValue().equals(shipping.getShippingStatus()) 
                && !ShippingStatusEnum.RECEIVED.getValue().equals(existingShipping.getShippingStatus())) {
            shipping.setReceiptTime(LocalDateTime.now());
            
            // 同时更新订单状态为已完成
            orderService.updateOrderStatus(existingShipping.getOrderId(), OrderStatusEnum.COMPLETED.getValue());
        }
        
        return shippingMapper.updateById(shipping) > 0;
    }
    
    /**
     * 确认收货
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @Transactional
    public boolean confirmReceipt(Long orderId, Long userId) {
        // 先确认订单收货
        if (!orderService.confirmReceipt(orderId, userId)) {
            return false;
        }
        
        // 更新发货信息状态
        Shipping shipping = getShippingByOrderId(orderId);
        if (shipping == null) {
            throw new ServiceException("发货信息不存在");
        }
        
        shipping.setShippingStatus(ShippingStatusEnum.RECEIVED.getValue());
        shipping.setReceiptTime(LocalDateTime.now());
        shipping.setUpdateTime(LocalDateTime.now());
        
        return shippingMapper.updateById(shipping) > 0;
    }
    
    /**
     * 生成发货单号
     * @return 发货单号
     */
    private String generateShippingNo() {
        // 发货单号格式：S + 时间戳 + 随机4位数
        return "S" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4);
    }
} 