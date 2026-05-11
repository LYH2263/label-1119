package com.huanshengxian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huanshengxian.common.PageResult;
import com.huanshengxian.common.Result;
import com.huanshengxian.entity.Coupon;
import com.huanshengxian.entity.UserCoupon;
import com.huanshengxian.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 优惠券控制器
 */
@Tag(name = "优惠券管理", description = "优惠券相关接口")
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "创建优惠券", description = "管理员创建新的优惠券，支持满减券、折扣券、无门槛券三种类型")
    @PostMapping
    public Result<Coupon> createCoupon(@RequestBody Coupon coupon) {
        Coupon created = couponService.createCoupon(coupon);
        return Result.success("创建成功", created);
    }

    @Operation(summary = "分页查询优惠券列表", description = "管理员分页查询所有优惠券，支持按名称、类型、状态筛选")
    @GetMapping
    public Result<PageResult<Coupon>> listCoupons(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "优惠券名称") @RequestParam(required = false) String name,
            @Parameter(description = "优惠券类型：1-满减券，2-折扣券，3-无门槛券") @RequestParam(required = false) Integer type,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam(required = false) Integer status) {
        
        Page<Coupon> result = couponService.pageCoupons(page, size, name, type, status);
        PageResult<Coupon> pageResult = PageResult.of(
                result.getTotal(),
                (int) result.getPages(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords()
        );
        
        return Result.success(pageResult);
    }

    @Operation(summary = "获取优惠券详情", description = "根据ID获取优惠券详细信息")
    @GetMapping("/{id}")
    public Result<Coupon> getCouponDetail(@Parameter(description = "优惠券ID") @PathVariable Long id) {
        Coupon coupon = couponService.getById(id);
        if (coupon == null) {
            return Result.error("优惠券不存在");
        }
        return Result.success(coupon);
    }

    @Operation(summary = "启用/禁用优惠券", description = "管理员启用或禁用优惠券，禁用后用户无法领取")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "优惠券ID") @PathVariable Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam Integer status) {
        
        boolean success = couponService.updateStatus(id, status);
        if (success) {
            return Result.success("状态更新成功", null);
        }
        return Result.error("状态更新失败");
    }

    @Operation(summary = "获取优惠券统计信息", description = "查看优惠券的领取和使用统计数据")
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> getCouponStats(@Parameter(description = "优惠券ID") @PathVariable Long id) {
        Map<String, Object> stats = couponService.getCouponStats(id);
        return Result.success(stats);
    }

    @Operation(summary = "查询可领取的优惠券列表", description = "用户查询当前可领取的优惠券列表")
    @GetMapping("/available")
    public Result<List<Coupon>> getAvailableCoupons(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        
        List<Coupon> coupons = couponService.getAvailableCoupons(userId);
        return Result.success(coupons);
    }

    @Operation(summary = "领取优惠券", description = "用户领取指定优惠券，每个用户每张券限领一次")
    @PostMapping("/{id}/claim")
    public Result<UserCoupon> claimCoupon(
            @Parameter(description = "优惠券ID") @PathVariable Long id,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        
        try {
            UserCoupon userCoupon = couponService.claimCoupon(userId, id);
            return Result.success("领取成功", userCoupon);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "查询我的优惠券", description = "用户分页查询自己的优惠券列表，支持按状态筛选")
    @GetMapping("/my")
    public Result<PageResult<UserCoupon>> getMyCoupons(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "状态：0-未使用，1-已使用，2-已过期") @RequestParam(required = false) Integer status) {
        
        Page<UserCoupon> result = couponService.pageUserCoupons(userId, page, size, status);
        PageResult<UserCoupon> pageResult = PageResult.of(
                result.getTotal(),
                (int) result.getPages(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords()
        );
        
        return Result.success(pageResult);
    }

    @Operation(summary = "查询订单可用优惠券", description = "订单结算时查询用户当前可用的优惠券列表")
    @GetMapping("/available-for-order")
    public Result<List<UserCoupon>> getAvailableCouponsForOrder(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "订单金额") @RequestParam BigDecimal orderAmount) {
        
        List<UserCoupon> coupons = couponService.getUserAvailableCoupons(userId, orderAmount);
        return Result.success(coupons);
    }

    @Operation(summary = "使用优惠券", description = "订单结算时使用优惠券，扣除优惠金额")
    @PostMapping("/{userCouponId}/use")
    public Result<BigDecimal> useCoupon(
            @Parameter(description = "用户优惠券ID") @PathVariable Long userCouponId,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "订单ID") @RequestParam Long orderId,
            @Parameter(description = "订单金额") @RequestParam BigDecimal orderAmount) {
        
        try {
            BigDecimal discountAmount = couponService.useCoupon(userCouponId, userId, orderId, orderAmount);
            return Result.success("使用成功", discountAmount);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "回滚优惠券使用", description = "订单创建失败时回滚优惠券状态")
    @PostMapping("/rollback")
    public Result<Void> rollbackCoupon(@Parameter(description = "订单ID") @RequestParam Long orderId) {
        boolean success = couponService.rollbackCoupon(orderId);
        if (success) {
            return Result.success("回滚成功", null);
        }
        return Result.error("回滚失败");
    }

    @Operation(summary = "计算优惠金额", description = "计算优惠券在指定订单金额下的优惠金额")
    @GetMapping("/{id}/calculate")
    public Result<BigDecimal> calculateDiscount(
            @Parameter(description = "优惠券ID") @PathVariable Long id,
            @Parameter(description = "订单金额") @RequestParam BigDecimal orderAmount) {
        
        Coupon coupon = couponService.getById(id);
        if (coupon == null) {
            return Result.error("优惠券不存在");
        }
        BigDecimal discountAmount = couponService.calculateDiscountAmount(coupon, orderAmount);
        return Result.success(discountAmount);
    }

    @Operation(summary = "更新过期优惠券", description = "定时任务调用，更新过期的优惠券状态")
    @PostMapping("/update-expired")
    public Result<Integer> updateExpiredCoupons() {
        int count = couponService.updateExpiredCoupons();
        return Result.success("处理了 " + count + " 张过期优惠券", count);
    }
}
