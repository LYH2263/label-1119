package com.huanshengxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huanshengxian.entity.CartItem;
import com.huanshengxian.entity.Product;
import com.huanshengxian.mapper.CartItemMapper;
import com.huanshengxian.mapper.ProductMapper;
import com.huanshengxian.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车服务实现
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartService {

    private final ProductMapper productMapper;

    @Override
    public List<CartItem> getCartByUserId(Long userId) {
        List<CartItem> cartItems = this.list(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getCreatedAt));
        
        // 填充商品信息
        cartItems.forEach(item -> {
            Product product = productMapper.selectById(item.getProductId());
            item.setProduct(product);
        });
        
        return cartItems;
    }

    @Override
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        // 检查是否已在购物车
        CartItem existing = this.getOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, productId));
        
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            this.updateById(existing);
            return existing;
        }
        
        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setProductId(productId);
        cartItem.setQuantity(quantity);
        cartItem.setSelected(1);
        
        this.save(cartItem);
        return cartItem;
    }

    @Override
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        CartItem cartItem = this.getOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, productId));
        
        if (cartItem != null) {
            cartItem.setQuantity(quantity);
            this.updateById(cartItem);
        }
    }

    @Override
    public void removeFromCart(Long userId, Long productId) {
        this.remove(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, productId));
    }

    @Override
    public void clearCart(Long userId) {
        this.remove(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId));
    }

    @Override
    public Integer getCartCount(Long userId) {
        return Math.toIntExact(this.count(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)));
    }
}
