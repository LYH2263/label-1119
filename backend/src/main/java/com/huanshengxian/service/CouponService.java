package com.huanshengxian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huanshengxian.dto.CouponCreateDTO;
import com.huanshengxian.entity.Coupon;
import com.huanshengxian.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 优惠券服务接口
 * 定义优惠券的业务操作方法，包括管理端的CRUD、用户端的领取和使用等
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 创建优惠券
     * @param coupon 优惠券信息
     * @return 创建后的优惠券
     */
    Coupon createCoupon(Coupon coupon);

    /**
     * 通过DTO创建优惠券
     * @param dto 优惠券创建请求参数
     * @return 创建后的优惠券
     */
    Coupon createCouponFromDTO(CouponCreateDTO dto);

    /**
     * 分页查询优惠券列表（管理端）
     * @param page 页码
     * @param size 每页数量
     * @param type 优惠券类型（可选）
     * @param status 优惠券状态（可选）
     * @return 分页结果
     */
    Page<Coupon> listCoupons(Integer page, Integer size, String type, Integer status);

    /**
     * 启用/禁用优惠券
     * @param couponId 优惠券ID
     * @param status 目标状态
     */
    void updateCouponStatus(Long couponId, Integer status);

    /**
     * 查看优惠券使用统计
     * @param couponId 优惠券ID
     * @return 统计信息
     */
    Map<String, Object> getCouponStats(Long couponId);

    /**
     * 查询可领取的优惠券列表（用户端）
     * @param userId 用户ID
     * @return 可领取的优惠券列表
     */
    List<Coupon> getAvailableCoupons(Long userId);

    /**
     * 领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 用户优惠券关联记录
     */
    UserCoupon receiveCoupon(Long userId, Long couponId);

    /**
     * 查询我的优惠券列表
     * @param userId 用户ID
     * @param status 状态: 0-未使用, 1-已使用, 2-已过期（可选，null查询全部）
     * @return 用户优惠券列表
     */
    List<UserCoupon> getMyCoupons(Long userId, Integer status);

    /**
     * 查询订单可用的优惠券列表
     * @param userId 用户ID
     * @param orderAmount 订单金额
     * @return 可用的优惠券列表
     */
    List<Coupon> getUsableCoupons(Long userId, BigDecimal orderAmount);

    /**
     * 计算优惠金额
     * @param userCouponId 用户优惠券ID
     * @param orderAmount 订单金额
     * @return 优惠金额
     */
    BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount);

    /**
     * 使用优惠券（订单支付成功后调用）
     * @param userCouponId 用户优惠券ID
     * @param orderId 订单ID
     */
    void useCoupon(Long userCouponId, Long orderId);

    /**
     * 回滚优惠券使用状态（订单创建失败时调用）
     * @param userCouponId 用户优惠券ID
     */
    void rollbackCoupon(Long userCouponId);

    /**
     * 过期处理：将已过期的未使用优惠券标记为已过期
     */
    void expireCoupons();
}
