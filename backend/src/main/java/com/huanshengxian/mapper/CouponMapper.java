package com.huanshengxian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huanshengxian.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 优惠券 Mapper 接口
 * 提供优惠券数据的数据库访问操作
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * 查询用户可领取的优惠券列表
     * @param userId 用户ID
     * @return 可领取的优惠券列表
     */
    List<Coupon> selectAvailableCoupons(@Param("userId") Long userId);

    /**
     * 查询优惠券使用统计
     * @param couponId 优惠券ID
     * @return 统计信息Map
     */
    Map<String, Object> selectCouponStats(@Param("couponId") Long couponId);

    /**
     * 查询订单可用的优惠券列表
     * @param userId 用户ID
     * @param orderAmount 订单金额
     * @return 可用的优惠券列表
     */
    List<Coupon> selectUsableCoupons(@Param("userId") Long userId, @Param("orderAmount") BigDecimal orderAmount);
}
