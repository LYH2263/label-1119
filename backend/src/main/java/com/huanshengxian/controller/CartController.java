package com.huanshengxian.controller;

import com.huanshengxian.common.Result;
import com.huanshengxian.entity.CartItem;
import com.huanshengxian.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@Tag(name = "购物车管理", description = "购物车相关接口")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "获取购物车")
    @GetMapping
    public Result<List<CartItem>> getCart(@RequestAttribute Long userId) {
        return Result.success(cartService.getCartByUserId(userId));
    }

    @Operation(summary = "添加到购物车")
    @PostMapping
    public Result<CartItem> addToCart(@RequestAttribute Long userId,
                                      @RequestBody Map<String, Object> params) {
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        
        CartItem item = cartService.addToCart(userId, productId, quantity);
        return Result.success("添加成功", item);
    }

    @Operation(summary = "更新购物车数量")
    @PutMapping("/{productId}")
    public Result<Void> updateQuantity(@RequestAttribute Long userId,
                                       @PathVariable Long productId,
                                       @RequestBody Map<String, Integer> params) {
        cartService.updateQuantity(userId, productId, params.get("quantity"));
        return Result.success();
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/{productId}")
    public Result<Void> removeFromCart(@RequestAttribute Long userId,
                                       @PathVariable Long productId) {
        cartService.removeFromCart(userId, productId);
        return Result.success();
    }

    @Operation(summary = "清空购物车")
    @DeleteMapping
    public Result<Void> clearCart(@RequestAttribute Long userId) {
        cartService.clearCart(userId);
        return Result.success();
    }

    @Operation(summary = "获取购物车数量")
    @GetMapping("/count")
    public Result<Integer> getCartCount(@RequestAttribute Long userId) {
        return Result.success(cartService.getCartCount(userId));
    }
}
