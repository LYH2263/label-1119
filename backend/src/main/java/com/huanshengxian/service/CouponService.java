package com.huanshengxian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huanshengxian.dto.CouponCalculateDTO;
import com.huanshengxian.dto.CouponCreateDTO;
import com.huanshengxian.entity.Coupon;
import com.huanshengxian.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券服务接口
 * 功能说明：提供优惠券创建、查询、领取、使用等功能
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 创建优惠券
     *
     * @param dto 创建优惠券请求参数
     * @return 创建的优惠券
     */
    Coupon createCoupon(CouponCreateDTO dto);

    /**
     * 分页查询优惠券列表（管理员）
     *
     * @param page 页码
     * @param size 每页数量
     * @param name 名称筛选（可选）
     * @param type 类型筛选（可选）
     * @return 优惠券分页列表
     */
    Page<Coupon> getCouponPage(Integer page, Integer size, String name, String type);

    /**
     * 启用/禁用优惠券
     *
     * @param id     优惠券ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取优惠券统计信息
     *
     * @param id 优惠券ID
     * @return 优惠券（包含统计数据）
     */
    Coupon getCouponStats(Long id);

    /**
     * 查询可领取的优惠券列表
     *
     * @return 可领取的优惠券列表
     */
    List<Coupon> getAvailableCoupons();

    /**
     * 领取优惠券
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @return 用户优惠券
     */
    UserCoupon claimCoupon(Long userId, Long couponId);

    /**
     * 分页查询我的优惠券
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @param status 状态筛选（可选）
     * @return 用户优惠券分页列表
     */
    Page<UserCoupon> getMyCoupons(Long userId, Integer page, Integer size, String status);

    /**
     * 计算优惠金额
     *
     * @param userId 用户ID
     * @param dto    计算请求参数
     * @return 优惠金额
     */
    BigDecimal calculateDiscount(Long userId, CouponCalculateDTO dto);

    /**
     * 使用优惠券（标记为已使用）
     *
     * @param userId      用户ID
     * @param userCouponId 用户优惠券ID
     * @param orderId     订单ID
     */
    void useCoupon(Long userId, Long userCouponId, Long orderId);

    /**
     * 回滚优惠券状态（订单创建失败时）
     *
     * @param userCouponId 用户优惠券ID
     */
    void rollbackCoupon(Long userCouponId);

    /**
     * 查询可用的优惠券（订单结算时）
     *
     * @param userId      用户ID
     * @param orderAmount 订单金额
     * @return 可用的用户优惠券列表
     */
    List<UserCoupon> getAvailableForOrder(Long userId, BigDecimal orderAmount);
}
