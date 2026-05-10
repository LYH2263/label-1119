package com.huanshengxian.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 优惠券优惠金额计算请求DTO
 * 用于订单结算时计算优惠金额的请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "优惠券优惠金额计算请求参数")
public class CouponCalculateDTO {

    /**
     * 用户优惠券ID
     */
    @Schema(description = "用户优惠券ID", example = "1", required = true)
    private Long userCouponId;

    /**
     * 订单金额
     */
    @Schema(description = "订单金额", example = "100.00", required = true)
    private BigDecimal orderAmount;
}
