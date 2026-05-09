package com.huanshengxian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huanshengxian.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券 Mapper 接口
 * 功能说明：提供优惠券数据访问层接口
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {
}
