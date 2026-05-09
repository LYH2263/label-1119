package com.huanshengxian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huanshengxian.common.PageResult;
import com.huanshengxian.common.Result;
import com.huanshengxian.entity.Order;
import com.huanshengxian.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@Tag(name = "订单管理", description = "订单相关接口")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<Order> createOrder(@RequestAttribute Long userId,
                                     @RequestBody Map<String, Object> params) {
        Long addressId = Long.valueOf(params.get("addressId").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";
        @SuppressWarnings("unchecked")
        List<Long> cartItemIds = ((List<Number>) params.get("cartItemIds"))
                .stream()
                .map(Number::longValue)
                .toList();
        
        try {
            Order order = orderService.createOrder(userId, addressId, remark, cartItemIds);
            return Result.success("订单创建成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取用户订单列表")
    @GetMapping
    public Result<PageResult<Order>> getUserOrders(@RequestAttribute Long userId,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "1") Integer page,
                                                   @RequestParam(defaultValue = "10") Integer size) {
        Page<Order> result = orderService.getUserOrders(userId, status, page, size);
        PageResult<Order> pageResult = PageResult.of(
                result.getTotal(),
                (int) result.getPages(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords()
        );
        return Result.success(pageResult);
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<Order> getOrderDetail(@PathVariable Long id) {
        Order order = orderService.getOrderDetail(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            return Result.success("订单已取消", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "支付订单")
    @PutMapping("/{id}/pay")
    public Result<Void> payOrder(@PathVariable Long id) {
        try {
            orderService.payOrder(id);
            return Result.success("支付成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "确认收货")
    @PutMapping("/{id}/receive")
    public Result<Void> confirmReceive(@PathVariable Long id) {
        try {
            orderService.confirmReceive(id);
            return Result.success("确认收货成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
