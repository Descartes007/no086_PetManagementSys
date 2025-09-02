package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.example.springboot.DTO.CartItemDTO;
import org.example.springboot.DTO.OrderCreateDTO;
import org.example.springboot.entity.Order;
import org.example.springboot.entity.Product;
import org.example.springboot.entity.Shipping;
import org.example.springboot.enumClass.OrderStatusEnum;
import org.example.springboot.enumClass.ShippingStatusEnum;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.OrderMapper;
import org.example.springboot.mapper.ProductMapper;
import org.example.springboot.mapper.ShippingMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 订单服务实现类
 */
@Service
public class OrderService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    
    @Resource
    private OrderMapper orderMapper;
    
    @Resource
    private ProductService productService;
    @Autowired
    private ShippingMapper shippingMapper;
    @Autowired
    private ProductMapper productMapper;
    
    @Value("${order.payment.timeout:15}")
    private Integer orderPaymentTimeout; // 默认15分钟

    /**
     * 创建订单
     * @param userId 用户ID
     * @param orderCreateDTO 订单创建DTO
     * @return 订单对象
     */
    @Transactional
    public Order createOrder(Long userId, OrderCreateDTO orderCreateDTO) {
        if (orderCreateDTO.getItems() == null || orderCreateDTO.getItems().isEmpty()) {
            throw new ServiceException("订单商品不能为空");
        }
        
        // 1. 校验收货信息
        if (StringUtils.isBlank(orderCreateDTO.getAddress())) {
            throw new ServiceException("收货地址不能为空");
        }
        if (StringUtils.isBlank(orderCreateDTO.getContactName())) {
            throw new ServiceException("联系人不能为空");
        }
        if (StringUtils.isBlank(orderCreateDTO.getContactPhone())) {
            throw new ServiceException("联系电话不能为空");
        }
        
        // 2. 生成订单号
        String orderNo = generateOrderNo();
        
        // 3. 遍历购物车项，创建订单
        List<CartItemDTO> items = orderCreateDTO.getItems();
        for (CartItemDTO item : items) {
            // 检查商品是否存在
            Product product = productService.getProductById(item.getProductId());
            if (product == null) {
                throw new ServiceException("商品不存在：" + item.getProductName());
            }
            
            // 检查商品是否上架
            if (product.getStatus() != 1) {
                throw new ServiceException("商品已下架：" + product.getName());
            }
            
            // 检查库存是否充足
            if (product.getStock() < item.getQuantity()) {
                throw new ServiceException("商品库存不足：" + product.getName());
            }
            
            // 创建订单对象
            Order order = new Order();
            order.setUserId(userId);
            order.setOrderNo(orderNo);
            order.setProductId(product.getId());
            order.setProductName(product.getName());
            if(product.getImages()!=null){
                order.setProductImage(product.getImages().split(",")[0]); // 取第一张图片
            }

            order.setQuantity(item.getQuantity());
            order.setPrice(product.getPrice());
            order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP));
            order.setPaymentMethod(orderCreateDTO.getPaymentMethod());
            order.setStatus(OrderStatusEnum.PENDING_PAYMENT.getValue());
            order.setAddress(orderCreateDTO.getAddress());
            order.setContactName(orderCreateDTO.getContactName());
            order.setContactPhone(orderCreateDTO.getContactPhone());
            order.setRemark(orderCreateDTO.getRemark());
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            
            // 设置支付截止时间
            order.setPaymentDeadline(LocalDateTime.now().plusMinutes(orderPaymentTimeout));
            
            // 保存订单
            if (orderMapper.insert(order) <= 0) {
                throw new ServiceException("创建订单失败");
            }
            
            // 扣减库存
            if (!productService.updateStock(product.getId(), -item.getQuantity())) {
                throw new ServiceException("库存更新失败：" + product.getName());
            }
        }
        
        // 查询刚创建的订单
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getOrderNo, orderNo);
        queryWrapper.eq(Order::getUserId, userId);
        queryWrapper.last("LIMIT 1");
        return orderMapper.selectOne(queryWrapper);
    }
    
    /**
     * 根据订单ID获取订单
     * @param id 订单ID
     * @return 订单对象
     */
    public Order getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        fillOrderInfo(order);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        return order;
    }
    
    /**
     * 根据用户ID和订单号获取订单
     * @param userId 用户ID
     * @param orderNo 订单号
     * @return 订单列表
     */
    public List<Order> getOrdersByUserIdAndOrderNo(Long userId, String orderNo) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId);
        queryWrapper.eq(StringUtils.isNotBlank(orderNo), Order::getOrderNo, orderNo);
        queryWrapper.orderByDesc(Order::getCreateTime);
        List<Order> orders = orderMapper.selectList(queryWrapper);
        orders.forEach(this::fillOrderInfo);
        return orders;
    }
    
    /**
     * 分页查询用户订单
     * @param userId 用户ID
     * @param status 订单状态
     * @param currentPage 当前页
     * @param size 每页大小
     * @return 分页订单列表
     */
    public Page<Order> getOrdersByPage(Long userId, String status, Integer currentPage, Integer size) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(userId != null, Order::getUserId, userId);
        queryWrapper.eq(StringUtils.isNotBlank(status), Order::getStatus, status);
        queryWrapper.orderByDesc(Order::getCreateTime);

        Page<Order> orderPage = orderMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
        orderPage.getRecords().forEach(this::fillOrderInfo);
        return orderPage;
    }

    /**
     * 填充订单信息
     * @param order 订单实体
     */

    private void fillOrderInfo(Order order) {
        if(order == null) return;
        
        if(order.getProductId()!=null){
            Product product = productMapper.selectById(order.getProductId());
            if(product != null) {
                order.setProductName(product.getName());
                if(product.getImages()!=null){
                    order.setProductImage(product.getImages().split(",")[0]);
                }
            }
        }
    }
    
    /**
     * 更新订单状态
     * @param id 订单ID
     * @param status 新状态
     * @return 是否成功
     */
    @Transactional
    public boolean updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        
        // 如果是支付成功，设置支付时间
        if (OrderStatusEnum.PENDING_DELIVERY.getValue().equals(status)) {
            order.setPaymentTime(LocalDateTime.now());
        }
        
        return orderMapper.updateById(order) > 0;
    }
    
    /**
     * 取消订单
     * @param id 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @Transactional
    public boolean cancelOrder(Long id, Long userId) {
        Order order = getOrderById(id);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 检查订单是否属于该用户
        if (!order.getUserId().equals(userId)) {
            throw new ServiceException("无权操作此订单");
        }
        
        // 只有待付款状态才能取消
        if (!OrderStatusEnum.PENDING_PAYMENT.getValue().equals(order.getStatus())) {
            throw new ServiceException("订单状态不允许取消");
        }
        
        // 更新订单状态
        order.setStatus(OrderStatusEnum.CANCELLED.getValue());
        order.setUpdateTime(LocalDateTime.now());
        
        boolean success = orderMapper.updateById(order) > 0;
        
        // 恢复商品库存
        if (success) {
            productService.updateStock(order.getProductId(), order.getQuantity());
        }
        
        return success;
    }
    
    /**
     * 确认收货
     * @param id 订单ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @Transactional
    public boolean confirmReceipt(Long id, Long userId) {
        Order order = getOrderById(id);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 检查订单是否属于该用户
        if (!order.getUserId().equals(userId)) {
            throw new ServiceException("无权操作此订单");
        }
        
        // 只有待收货状态才能确认收货
        if (!OrderStatusEnum.PENDING_RECEIPT.getValue().equals(order.getStatus())) {
            throw new ServiceException("订单状态不允许确认收货");
        }
        // 更新发货信息状态
        List<Shipping> shippings = shippingMapper.selectList(new LambdaQueryWrapper<Shipping>().eq(Shipping::getOrderId, id));
        if(!shippings.isEmpty()){
            Shipping shipping = shippings.get(0);

            shipping.setShippingStatus(ShippingStatusEnum.RECEIVED.getValue());
            shipping.setReceiptTime(LocalDateTime.now());
            shipping.setUpdateTime(LocalDateTime.now());
            shippingMapper.updateById(shipping);
        }


        // 更新订单状态
        order.setStatus(OrderStatusEnum.COMPLETED.getValue());

        order.setUpdateTime(LocalDateTime.now());
        
        return orderMapper.updateById(order) > 0;
    }
    
    /**
     * 删除订单
     * @param id 订单ID
     * @return 是否成功
     */
    @Transactional
    public boolean deleteOrder(Long id) {
        Order order = getOrderById(id);
        if (order == null) {
            throw new ServiceException("订单不存在");
        }
        
        // 只有已完成或已取消的订单才能删除
        if (!OrderStatusEnum.COMPLETED.getValue().equals(order.getStatus()) 
                && !OrderStatusEnum.CANCELLED.getValue().equals(order.getStatus())) {
            throw new ServiceException("订单状态不允许删除");
        }
        
        return orderMapper.deleteById(id) > 0;
    }
    
    /**
     * 生成订单号
     * @return 订单号
     */
    private String generateOrderNo() {
        // 订单号格式：时间戳 + 随机4位数
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 4);
        return timestamp + random;
    }
    
    /**
     * 检查订单是否已过期（超过支付截止时间）
     * @param order 订单对象
     * @return 是否已过期
     */
    public boolean isOrderExpired(Order order) {
        if (order == null || order.getPaymentDeadline() == null) {
            return false;
        }
        
        // 只检查待付款状态的订单
        if (!OrderStatusEnum.PENDING_PAYMENT.getValue().equals(order.getStatus())) {
            return false;
        }
        
        return LocalDateTime.now().isAfter(order.getPaymentDeadline());
    }
    
    /**
     * 获取剩余支付时间（秒）
     * @param order 订单对象
     * @return 剩余秒数，如果已过期则返回0
     */
    public long getRemainingPaymentTime(Order order) {
        if (order == null || order.getPaymentDeadline() == null) {
            return 0;
        }
        
        if (!OrderStatusEnum.PENDING_PAYMENT.getValue().equals(order.getStatus())) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(order.getPaymentDeadline())) {
            return 0;
        }
        
        return java.time.Duration.between(now, order.getPaymentDeadline()).getSeconds();
    }
    
    /**
     * 定时任务：自动取消超时未支付的订单
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    @Transactional
    public void autoCloseExpiredOrders() {
        LOGGER.info("开始检查超时未支付订单...");
        
        try {
            // 查询所有待付款状态的订单
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getStatus, OrderStatusEnum.PENDING_PAYMENT.getValue());
            queryWrapper.lt(Order::getPaymentDeadline, LocalDateTime.now()); // 支付截止时间早于当前时间
            
            List<Order> expiredOrders = orderMapper.selectList(queryWrapper);
            
            if (expiredOrders.isEmpty()) {
                LOGGER.info("没有超时未支付订单");
                return;
            }
            
            LOGGER.info("发现 {} 个超时未支付订单，开始自动取消", expiredOrders.size());
            
            for (Order order : expiredOrders) {
                try {
                    // 更新订单状态为已取消
                    order.setStatus(OrderStatusEnum.CANCELLED.getValue());
                    order.setUpdateTime(LocalDateTime.now());
                    order.setRemark((order.getRemark() != null ? order.getRemark() + " " : "") + "系统自动取消：超时未支付");
                    
                    boolean updated = orderMapper.updateById(order) > 0;
                    
                    if (updated) {
                        // 恢复商品库存
                        productService.updateStock(order.getProductId(), order.getQuantity());
                        LOGGER.info("已自动取消订单：{}", order.getOrderNo());
                    } else {
                        LOGGER.error("自动取消订单失败：{}", order.getOrderNo());
                    }
                } catch (Exception e) {
                    LOGGER.error("自动取消订单异常：{}", order.getOrderNo(), e);
                }
            }
            
            LOGGER.info("超时未支付订单处理完成");
        } catch (Exception e) {
            LOGGER.error("自动取消订单任务执行异常", e);
        }
    }
} 