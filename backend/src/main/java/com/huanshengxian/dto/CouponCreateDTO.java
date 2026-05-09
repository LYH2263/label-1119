package com.huanshengxian.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建优惠券请求 DTO
 * 功能说明：封装创建优惠券的请求参数
 */
@Data
public class CouponCreateDTO {

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 优惠券类型
     */
    private String type;

    /**
     * 面额/折扣值
     */
    private BigDecimal value;

    /**
     * 使用门槛
     */
    private BigDecimal minAmount;

    /**
     * 发放总量
     */
    private Integer totalCount;

    /**
     * 有效期开始时间
     */
    private LocalDateTime startTime;

    /**
     * 有效期结束时间
     */
    private LocalDateTime endTime;
}
