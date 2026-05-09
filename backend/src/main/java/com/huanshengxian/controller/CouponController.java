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

/**
 * 优惠券控制器
 * 功能说明：
 * - 提供优惠券管理（管理员）接口
 * - 提供优惠券领取、查询、使用（用户）接口
 */
@Tag(name = "优惠券管理", description = "优惠券相关接口")
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /**
     * 创建优惠券（管理员）
     *
     * @param dto 创建优惠券请求参数
     * @return 创建的优惠券
     */
    @Operation(summary = "创建优惠券", description = "管理员创建新的优惠券，支持满减券、折扣券、无门槛券三种类型")
    @PostMapping
    public Result<Coupon> createCoupon(@RequestBody CouponCreateDTO dto) {
        try {
            Coupon coupon = couponService.createCoupon(dto);
            return Result.success("优惠券创建成功", coupon);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询优惠券列表（管理员）
     *
     * @param page 页码
     * @param size 每页数量
     * @param name 名称筛选（可选）
     * @param type 类型筛选（可选）
     * @return 优惠券分页列表
     */
    @Operation(summary = "查询优惠券列表", description = "管理员分页查询优惠券列表，支持按名称和类型筛选")
    @GetMapping
    public Result<PageResult<Coupon>> getCouponPage(@RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size,
                                                    @RequestParam(required = false) String name,
                                                    @RequestParam(required = false) String type) {
        Page<Coupon> result = couponService.getCouponPage(page, size, name, type);
        PageResult<Coupon> pageResult = PageResult.of(
                result.getTotal(),
                (int) result.getPages(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords()
        );
        return Result.success(pageResult);
    }

    /**
     * 启用/禁用优惠券（管理员）
     *
     * @param id     优惠券ID
     * @param status 状态（1-启用，0-禁用）
     * @return 操作结果
     */
    @Operation(summary = "启用/禁用优惠券", description = "管理员启用或禁用优惠券，status=1启用，status=0禁用")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        try {
            couponService.updateStatus(id, status);
            return Result.success("状态更新成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查看优惠券统计（管理员）
     *
     * @param id 优惠券ID
     * @return 优惠券（包含统计数据）
     */
    @Operation(summary = "查看优惠券统计", description = "查看优惠券的发放总量、已领取数量、已使用数量等统计信息")
    @GetMapping("/{id}/stats")
    public Result<Coupon> getCouponStats(@PathVariable Long id) {
        Coupon coupon = couponService.getCouponStats(id);
        if (coupon == null) {
            return Result.error("优惠券不存在");
        }
        return Result.success(coupon);
    }

    /**
     * 查询可领取的优惠券列表（用户）
     *
     * @return 可领取的优惠券列表
     */
    @Operation(summary = "查询可领取的优惠券", description = "查询当前可以领取的优惠券列表")
    @GetMapping("/available")
    public Result<List<Coupon>> getAvailableCoupons() {
        return Result.success(couponService.getAvailableCoupons());
    }

    /**
     * 领取优惠券（用户）
     *
     * @param userId   当前用户ID（从JWT中获取）
     * @param couponId 优惠券ID
     * @return 用户优惠券
     */
    @Operation(summary = "领取优惠券", description = "用户领取优惠券，每个用户每张券限领一次")
    @PostMapping("/{couponId}/claim")
    public Result<UserCoupon> claimCoupon(@RequestAttribute Long userId,
                                          @PathVariable Long couponId) {
        try {
            UserCoupon userCoupon = couponService.claimCoupon(userId, couponId);
            return Result.success("领取成功", userCoupon);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 分页查询我的优惠券（用户）
     *
     * @param userId 当前用户ID
     * @param page   页码
     * @param size   每页数量
     * @param status 状态筛选（可选）：UNUSED-未使用、USED-已使用、EXPIRED-已过期
     * @return 我的优惠券分页列表
     */
    @Operation(summary = "查询我的优惠券", description = "分页查询用户自己的优惠券，支持按状态筛选")
    @GetMapping("/my")
    public Result<PageResult<UserCoupon>> getMyCoupons(@RequestAttribute Long userId,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer size,
                                                       @RequestParam(required = false) String status) {
        Page<UserCoupon> result = couponService.getMyCoupons(userId, page, size, status);
        PageResult<UserCoupon> pageResult = PageResult.of(
                result.getTotal(),
                (int) result.getPages(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords()
        );
        return Result.success(pageResult);
    }

    /**
     * 查询订单可用优惠券（用户）
     *
     * @param userId      当前用户ID
     * @param orderAmount 订单金额
     * @return 可用的用户优惠券列表
     */
    @Operation(summary = "查询订单可用优惠券", description = "在订单结算时，根据订单金额查询可用的优惠券")
    @GetMapping("/for-order")
    public Result<List<UserCoupon>> getAvailableForOrder(@RequestAttribute Long userId,
                                                         @RequestParam BigDecimal orderAmount) {
        return Result.success(couponService.getAvailableForOrder(userId, orderAmount));
    }

    /**
     * 计算优惠金额（用户）
     *
     * @param userId 当前用户ID
     * @param dto    计算请求参数（包含用户优惠券ID和订单金额）
     * @return 优惠金额
     */
    @Operation(summary = "计算优惠金额", description = "计算使用指定优惠券后的优惠金额")
    @PostMapping("/calculate")
    public Result<BigDecimal> calculateDiscount(@RequestAttribute Long userId,
                                                @RequestBody CouponCalculateDTO dto) {
        try {
            BigDecimal discount = couponService.calculateDiscount(userId, dto);
            return Result.success(discount);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 使用优惠券（用户）
     *
     * @param userId       当前用户ID
     * @param userCouponId 用户优惠券ID
     * @param orderId      订单ID
     * @return 操作结果
     */
    @Operation(summary = "使用优惠券", description = "订单支付成功后，标记优惠券为已使用")
    @PutMapping("/use/{userCouponId}")
    public Result<Void> useCoupon(@RequestAttribute Long userId,
                                  @PathVariable Long userCouponId,
                                  @RequestParam Long orderId) {
        try {
            couponService.useCoupon(userId, userCouponId, orderId);
            return Result.success("优惠券使用成功", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 回滚优惠券状态（内部接口）
     *
     * @param userCouponId 用户优惠券ID
     * @return 操作结果
     */
    @Operation(summary = "回滚优惠券状态", description = "订单创建失败时，回滚优惠券状态为未使用")
    @PutMapping("/rollback/{userCouponId}")
    public Result<Void> rollbackCoupon(@PathVariable Long userCouponId) {
        try {
            couponService.rollbackCoupon(userCouponId);
            return Result.success("优惠券已回滚", null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
