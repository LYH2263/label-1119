package com.huanshengxian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huanshengxian.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户优惠券Mapper接口
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
    
    /**
     * 分页查询用户优惠券
     * @param page 分页参数
     * @param userId 用户ID
     * @param status 状态
     * @return 分页结果
     */
    Page<UserCoupon> selectUserCouponPage(Page<UserCoupon> page, 
                                           @Param("userId") Long userId, 
                                           @Param("status") Integer status);
    
    /**
     * 查询用户可用的优惠券（带优惠券详情）
     * @param userId 用户ID
     * @param orderAmount 订单金额
     * @param now 当前时间
     * @return 用户优惠券列表
     */
    List<UserCoupon> selectAvailableCouponsWithDetail(@Param("userId") Long userId, 
                                                       @Param("orderAmount") BigDecimal orderAmount, 
                                                       @Param("now") LocalDateTime now);
    
    /**
     * 更新过期的优惠券状态
     * @param now 当前时间
     * @return 影响行数
     */
    int updateExpiredCoupons(@Param("now") LocalDateTime now);
    
    /**
     * 根据订单ID查询用户优惠券
     * @param orderId 订单ID
     * @return 用户优惠券
     */
    UserCoupon selectByOrderId(@Param("orderId") Long orderId);
}
