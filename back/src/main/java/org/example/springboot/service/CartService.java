package org.example.springboot.service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.example.springboot.DTO.CartDTO;
import org.example.springboot.DTO.CartItemDTO;
import org.example.springboot.entity.Product;
import org.example.springboot.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 */
@Service
public class CartService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CartService.class);
    private static final String CART_SESSION_KEY = "userCart";
    
    @Resource
    private ProductService productService;
    
    /**
     * 添加商品到购物车
     * @param session HTTP会话
     * @param productId 商品ID
     * @param quantity 数量
     * @return 购物车DTO
     */
    public CartDTO addToCart(HttpSession session, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new ServiceException("商品数量必须大于0");
        }
        
        // 获取商品信息
        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        
        // 检查商品是否上架
        if (product.getStatus() != 1) {
            throw new ServiceException("商品已下架");
        }
        
        // 检查库存是否充足
        if (product.getStock() < quantity) {
            throw new ServiceException("商品库存不足");
        }
        
        // 从会话中获取购物车，如果不存在则创建
        Map<Long, CartItemDTO> cartItems = getCartItemsFromSession(session);
        
        // 如果购物车中已经存在该商品，则更新数量
        if (cartItems.containsKey(productId)) {
            CartItemDTO item = cartItems.get(productId);
            int newQuantity = item.getQuantity() + quantity;
            
            // 再次检查库存是否充足
            if (product.getStock() < newQuantity) {
                throw new ServiceException("商品库存不足");
            }
            
            item.setQuantity(newQuantity);
            item.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(newQuantity)).setScale(2, RoundingMode.HALF_UP));
        } else {
            // 创建新的购物车项
            CartItemDTO item = new CartItemDTO();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            if(product.getImages()!=null){
                item.setProductImage(product.getImages().split(",")[0]); // 取第一张图片
            }

            item.setPrice(product.getPrice());
            item.setQuantity(quantity);
            item.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));
            cartItems.put(productId, item);
        }
        
        // 保存购物车到会话
        session.setAttribute(CART_SESSION_KEY, cartItems);
        
        // 返回购物车DTO
        return getCart(session);
    }
    
    /**
     * 更新购物车商品数量
     * @param session HTTP会话
     * @param productId 商品ID
     * @param quantity 新数量
     * @return 购物车DTO
     */
    public CartDTO updateCartItemQuantity(HttpSession session, Long productId, Integer quantity) {
        if (quantity <= 0) {
            // 如果数量小于等于0，则移除该商品
            return removeFromCart(session, productId);
        }
        
        // 获取商品信息
        Product product = productService.getProductById(productId);
        if (product == null) {
            throw new ServiceException("商品不存在");
        }
        
        // 检查库存是否充足
        if (product.getStock() < quantity) {
            throw new ServiceException("商品库存不足");
        }
        
        // 从会话中获取购物车
        Map<Long, CartItemDTO> cartItems = getCartItemsFromSession(session);
        
        // 如果购物车中不存在该商品，则抛出异常
        if (!cartItems.containsKey(productId)) {
            throw new ServiceException("购物车中不存在该商品");
        }
        
        // 更新商品数量
        CartItemDTO item = cartItems.get(productId);
        item.setQuantity(quantity);
        item.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));
        
        // 保存购物车到会话
        session.setAttribute(CART_SESSION_KEY, cartItems);
        
        // 返回购物车DTO
        return getCart(session);
    }
    
    /**
     * 从购物车移除商品
     * @param session HTTP会话
     * @param productId 商品ID
     * @return 购物车DTO
     */
    public CartDTO removeFromCart(HttpSession session, Long productId) {
        // 从会话中获取购物车
        Map<Long, CartItemDTO> cartItems = getCartItemsFromSession(session);
        
        // 移除商品
        cartItems.remove(productId);
        
        // 保存购物车到会话
        session.setAttribute(CART_SESSION_KEY, cartItems);
        
        // 返回购物车DTO
        return getCart(session);
    }
    
    /**
     * 清空购物车
     * @param session HTTP会话
     * @return 空购物车DTO
     */
    public CartDTO clearCart(HttpSession session) {
        // 移除会话中的购物车
        session.removeAttribute(CART_SESSION_KEY);
        
        // 返回空购物车
        CartDTO cartDTO = new CartDTO();
        cartDTO.setItems(new ArrayList<>());
        cartDTO.setTotalQuantity(0);
        cartDTO.setTotalAmount(BigDecimal.ZERO);
        return cartDTO;
    }
    
    /**
     * 获取购物车
     * @param session HTTP会话
     * @return 购物车DTO
     */
    public CartDTO getCart(HttpSession session) {
        // 从会话中获取购物车
        Map<Long, CartItemDTO> cartItems = getCartItemsFromSession(session);
        
        // 转换为列表
        List<CartItemDTO> itemList = new ArrayList<>(cartItems.values());
        
        // 计算总数量和总金额
        int totalQuantity = itemList.stream().mapToInt(CartItemDTO::getQuantity).sum();
        BigDecimal totalAmount = itemList.stream()
                .map(CartItemDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        
        // 创建购物车DTO
        CartDTO cartDTO = new CartDTO();
        cartDTO.setItems(itemList);
        cartDTO.setTotalQuantity(totalQuantity);
        cartDTO.setTotalAmount(totalAmount);
        
        return cartDTO;
    }
    
    /**
     * 从会话获取购物车项
     * @param session HTTP会话
     * @return 购物车项Map
     */
    @SuppressWarnings("unchecked")
    private Map<Long, CartItemDTO> getCartItemsFromSession(HttpSession session) {
        Map<Long, CartItemDTO> cartItems = (Map<Long, CartItemDTO>) session.getAttribute(CART_SESSION_KEY);
        if (cartItems == null) {
            cartItems = new ConcurrentHashMap<>();
        }
        return cartItems;
    }
} 