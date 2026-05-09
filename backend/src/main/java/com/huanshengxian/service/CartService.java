package com.huanshengxian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huanshengxian.entity.CartItem;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService extends IService<CartItem> {
    
    /**
     * 获取用户购物车
     */
    List<CartItem> getCartByUserId(Long userId);
    
    /**
     * 添加商品到购物车
     */
    CartItem addToCart(Long userId, Long productId, Integer quantity);
    
    /**
     * 更新购物车数量
     */
    void updateQuantity(Long userId, Long productId, Integer quantity);
    
    /**
     * 删除购物车项
     */
    void removeFromCart(Long userId, Long productId);
    
    /**
     * 清空购物车
     */
    void clearCart(Long userId);
    
    /**
     * 获取购物车商品数量
     */
    Integer getCartCount(Long userId);
}
