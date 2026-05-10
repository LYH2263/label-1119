package com.huanshengxian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huanshengxian.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户优惠券关联 Mapper 接口
 * 提供用户优惠券关联数据的数据库访问操作
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /**
     * 查询用户的优惠券列表（含优惠券详情）
     * @param userId 用户ID
     * @param status 优惠券状态: 0-未使用, 1-已使用, 2-已过期
     * @return 用户优惠券列表
     */
    List<UserCoupon> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 统计用户已领取某优惠券的数量
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 已领取数量
     */
    Integer countByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);
}
