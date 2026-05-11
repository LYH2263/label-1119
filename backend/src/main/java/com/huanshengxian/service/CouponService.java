package com.huanshengxian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huanshengxian.entity.Coupon;
import com.huanshengxian.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 优惠券服务接口
 */
public interface CouponService extends IService<Coupon> {
    
    /**
     * 创建优惠券
     * @param coupon 优惠券信息
     * @return 创建后的优惠券
     */
    Coupon createCoupon(Coupon coupon);
    
    /**
     * 分页查询优惠券列表
     * @param page 页码
     * @param size 每页条数
     * @param name 优惠券名称
     * @param type 优惠券类型
     * @param status 状态
     * @return 分页结果
     */
    Page<Coupon> pageCoupons(Integer page, Integer size, String name, Integer type, Integer status);
    
    /**
     * 启用/禁用优惠券
     * @param id 优惠券ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
    
    /**
     * 获取优惠券统计信息
     * @param id 优惠券ID
     * @return 统计信息
     */
    Map<String, Object> getCouponStats(Long id);
    
    /**
     * 查询可领取的优惠券列表
     * @param userId 用户ID
     * @return 优惠券列表
     */
    List<Coupon> getAvailableCoupons(Long userId);
    
    /**
     * 领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 用户优惠券
     */
    UserCoupon claimCoupon(Long userId, Long couponId);
    
    /**
     * 分页查询用户优惠券
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页条数
     * @param status 状态
     * @return 分页结果
     */
    Page<UserCoupon> pageUserCoupons(Long userId, Integer page, Integer size, Integer status);
    
    /**
     * 查询用户可用的优惠券（订单结算时使用）
     * @param userId 用户ID
     * @param orderAmount 订单金额
     * @return 用户优惠券列表
     */
    List<UserCoupon> getUserAvailableCoupons(Long userId, BigDecimal orderAmount);
    
    /**
     * 使用优惠券
     * @param userCouponId 用户优惠券ID
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param orderAmount 订单金额
     * @return 优惠金额
     */
    BigDecimal useCoupon(Long userCouponId, Long userId, Long orderId, BigDecimal orderAmount);
    
    /**
     * 回滚优惠券使用（订单创建失败时）
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean rollbackCoupon(Long orderId);
    
    /**
     * 计算优惠金额
     * @param coupon 优惠券
     * @param orderAmount 订单金额
     * @return 优惠金额
     */
    BigDecimal calculateDiscountAmount(Coupon coupon, BigDecimal orderAmount);
    
    /**
     * 更新过期的优惠券状态
     * @return 处理的数量
     */
    int updateExpiredCoupons();
}
