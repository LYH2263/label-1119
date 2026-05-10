package com.huanshengxian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huanshengxian.common.PageResult;
import com.huanshengxian.common.Result;
import com.huanshengxian.dto.CouponCalculateDTO;
import com.huanshengxian.dto.CouponCreateDTO;
import com.huanshengxian.entity.Coupon;
import com.huanshengxian.entity.UserCoupon;
import com.huanshengxian.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 优惠券控制器
 * 提供优惠券的管理端和用户端接口，包括创建、查询、领取、使用等操作
 */
@Tag(name = "优惠券管理", description = "优惠券相关接口，包含管理端CRUD和用户端领取使用功能")
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "创建优惠券", description = "管理员创建新的优惠券，支持满减券、折扣券、无门槛券三种类型")
    @PostMapping
    public Result<Coupon> createCoupon(@RequestBody CouponCreateDTO dto) {
        try {
            Coupon created = couponService.createCouponFromDTO(dto);
            return Result.success("优惠券创建成功", created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "分页查询优惠券列表", description = "管理员分页查询优惠券列表，可按类型和状态筛选")
    @GetMapping("/list")
    public Result<PageResult<Coupon>> listCoupons(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status) {
        Page<Coupon> result = couponService.listCoupons(page, size, type, status);
        PageResult<Coupon> pageResult = PageResult.of(
                result.getTotal(),
                (int) result.getPages(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords()
        );
        return Result.success(pageResult);
    }

    @Operation(summary = "启用/禁用优惠券", description = "管理员切换优惠券的启用/禁用状态，0-禁用，1-启用")
    @PutMapping("/{id}/status")
    public Result<Void> updateCouponStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            couponService.updateCouponStatus(id, status);
            return Result.success("状态更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "查看优惠券使用统计", description = "管理员查看指定优惠券的领取数量、使用数量、使用率等统计信息")
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> getCouponStats(@PathVariable Long id) {
        try {
            Map<String, Object> stats = couponService.getCouponStats(id);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "查看可领取的优惠券列表", description = "用户查看当前可以领取的优惠券列表，已领取的优惠券会标记received字段")
    @GetMapping("/available")
    public Result<List<Coupon>> getAvailableCoupons(@RequestAttribute Long userId) {
        List<Coupon> coupons = couponService.getAvailableCoupons(userId);
        return Result.success(coupons);
    }

    @Operation(summary = "领取优惠券", description = "用户领取指定优惠券，每个用户每张券限领一次，需校验有效期和库存")
    @PostMapping("/{id}/receive")
    public Result<UserCoupon> receiveCoupon(@RequestAttribute Long userId, @PathVariable Long id) {
        try {
            UserCoupon userCoupon = couponService.receiveCoupon(userId, id);
            return Result.success("领取成功", userCoupon);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "查看我的优惠券", description = "用户查看自己领取的优惠券列表，可按状态筛选：0-未使用，1-已使用，2-已过期")
    @GetMapping("/mine")
    public Result<List<UserCoupon>> getMyCoupons(
            @RequestAttribute Long userId,
            @RequestParam(required = false) Integer status) {
        List<UserCoupon> coupons = couponService.getMyCoupons(userId, status);
        return Result.success(coupons);
    }

    @Operation(summary = "查询订单可用优惠券", description = "在订单结算时查询当前订单金额可使用的优惠券列表")
    @GetMapping("/usable")
    public Result<List<Coupon>> getUsableCoupons(
            @RequestAttribute Long userId,
            @RequestParam BigDecimal orderAmount) {
        List<Coupon> coupons = couponService.getUsableCoupons(userId, orderAmount);
        return Result.success(coupons);
    }

    @Operation(summary = "计算优惠金额", description = "根据用户优惠券ID和订单金额计算优惠金额，用于订单结算时展示")
    @PostMapping("/calculate")
    public Result<BigDecimal> calculateDiscount(@RequestBody CouponCalculateDTO dto) {
        try {
            BigDecimal discount = couponService.calculateDiscount(dto.getUserCouponId(), dto.getOrderAmount());
            return Result.success(discount);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "使用优惠券", description = "订单支付成功后标记优惠券为已使用，绑定订单ID")
    @PostMapping("/use")
    public Result<Void> useCoupon(@RequestBody Map<String, Object> params) {
        try {
            Long userCouponId = Long.valueOf(params.get("userCouponId").toString());
            Long orderId = Long.valueOf(params.get("orderId").toString());
            couponService.useCoupon(userCouponId, orderId);
            return Result.success("优惠券已使用", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "回滚优惠券使用", description = "订单创建失败时回滚优惠券状态为未使用，需在事务中调用")
    @PostMapping("/rollback")
    public Result<Void> rollbackCoupon(@RequestBody Map<String, Object> params) {
        try {
            Long userCouponId = Long.valueOf(params.get("userCouponId").toString());
            couponService.rollbackCoupon(userCouponId);
            return Result.success("优惠券已回滚", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "过期处理", description = "将已过期但未使用的优惠券批量标记为已过期状态")
    @PostMapping("/expire")
    public Result<Void> expireCoupons() {
        try {
            couponService.expireCoupons();
            return Result.success("过期处理完成", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
