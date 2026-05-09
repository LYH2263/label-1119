package com.huanshengxian.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 计算优惠金额请求 DTO
 * 功能说明：封装计算优惠券优惠金额的请求参数
 */
@Data
public class CouponCalculateDTO {

    /**
     * 用户优惠券ID
     */
    private Long userCouponId;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;
}
