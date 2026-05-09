package com.huanshengxian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huanshengxian.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户优惠券 Mapper 接口
 * 功能说明：提供用户优惠券数据访问层接口
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /**
     * 分页查询用户优惠券列表（关联优惠券信息）
     *
     * @param page   分页对象
     * @param userId 用户ID
     * @param status 状态筛选（可选）
     * @return 用户优惠券列表（包含优惠券详情）
     */
    @Select("SELECT uc.*, c.name, c.type, c.value, c.min_amount, c.start_time, c.end_time " +
            "FROM user_coupons uc " +
            "LEFT JOIN coupons c ON uc.coupon_id = c.id " +
            "WHERE uc.user_id = #{userId} " +
            "AND (#{status} IS NULL OR uc.status = #{status}) " +
            "AND uc.deleted_at IS NULL " +
            "ORDER BY uc.created_at DESC")
    List<UserCoupon> selectUserCouponsWithCoupon(Page<UserCoupon> page,
                                                 @Param("userId") Long userId,
                                                 @Param("status") String status);
}
