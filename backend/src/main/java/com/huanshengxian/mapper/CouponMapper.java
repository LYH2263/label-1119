package com.huanshengxian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huanshengxian.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券Mapper接口
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
    
    /**
     * 分页查询优惠券列表
     * @param page 分页参数
     * @param name 优惠券名称
     * @param type 优惠券类型
     * @param status 状态
     * @return 分页结果
     */
    Page<Coupon> selectCouponPage(Page<Coupon> page, 
                                    @Param("name") String name, 
                                    @Param("type") Integer type, 
                                    @Param("status") Integer status);
    
    /**
     * 查询可领取的优惠券列表
     * @param userId 用户ID
     * @param now 当前时间
     * @return 优惠券列表
     */
    List<Coupon> selectAvailableCoupons(@Param("userId") Long userId, 
                                         @Param("now") LocalDateTime now);
    
    /**
     * 查询用户可用的优惠券
     * @param userId 用户ID
     * @param orderAmount 订单金额
     * @param now 当前时间
     * @return 用户优惠券列表
     */
    List<Coupon> selectUserAvailableCoupons(@Param("userId") Long userId, 
                                             @Param("orderAmount") BigDecimal orderAmount, 
                                             @Param("now") LocalDateTime now);
    
    /**
     * 更新已领取数量
     * @param id 优惠券ID
     * @return 影响行数
     */
    int incrementReceivedCount(@Param("id") Long id);
    
    /**
     * 更新已使用数量
     * @param id 优惠券ID
     * @return 影响行数
     */
    int incrementUsedCount(@Param("id") Long id);
    
    /**
     * 扣减已使用数量
     * @param id 优惠券ID
     * @return 影响行数
     */
    int decrementUsedCount(@Param("id") Long id);
}
