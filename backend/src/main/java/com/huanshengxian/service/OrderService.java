package com.huanshengxian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huanshengxian.entity.Order;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {
    
    /**
     * 创建订单
     */
    Order createOrder(Long userId, Long addressId, String remark, List<Long> cartItemIds);
    
    /**
     * 获取用户订单列表
     */
    Page<Order> getUserOrders(Long userId, String status, Integer page, Integer size);
    
    /**
     * 获取订单详情
     */
    Order getOrderDetail(Long orderId);
    
    /**
     * 取消订单
     */
    void cancelOrder(Long orderId);
    
    /**
     * 支付订单
     */
    void payOrder(Long orderId);
    
    /**
     * 确认收货
     */
    void confirmReceive(Long orderId);
}
