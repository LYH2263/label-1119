package com.huanshengxian.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 优惠券创建请求DTO
 * 用于接收管理员创建优惠券时的请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "优惠券创建请求参数")
public class CouponCreateDTO {

    /**
     * 优惠券名称
     */
    @Schema(description = "优惠券名称", example = "新人专享满减券", required = true)
    private String name;

    /**
     * 优惠券类型: FULL_REDUCTION-满减券, DISCOUNT-折扣券, NO_THRESHOLD-无门槛券
     */
    @Schema(description = "优惠券类型: FULL_REDUCTION-满减券, DISCOUNT-折扣券, NO_THRESHOLD-无门槛券", example = "FULL_REDUCTION", required = true)
    private String type;

    /**
     * 面额/折扣值: 满减券为减免金额, 折扣券为折扣率(如8.5表示85折), 无门槛券为减免金额
     */
    @Schema(description = "面额/折扣值: 满减券为减免金额, 折扣券为折扣率(如8.5表示85折), 无门槛券为减免金额", example = "10.00", required = true)
    private BigDecimal discountValue;

    /**
     * 使用门槛金额: 满减券需要满足的最低订单金额, 无门槛券和折扣券为0或不传
     */
    @Schema(description = "使用门槛金额: 满减券需要满足的最低订单金额, 无门槛券可不传", example = "50.00")
    private BigDecimal minAmount;

    /**
     * 发放总量
     */
    @Schema(description = "发放总量", example = "1000", required = true)
    private Integer totalCount;

    /**
     * 有效期开始时间，格式: yyyy-MM-dd HH:mm:ss
     */
    @Schema(description = "有效期开始时间，格式: yyyy-MM-dd HH:mm:ss", example = "2024-01-01 00:00:00", required = true)
    private String startTime;

    /**
     * 有效期结束时间，格式: yyyy-MM-dd HH:mm:ss
     */
    @Schema(description = "有效期结束时间，格式: yyyy-MM-dd HH:mm:ss", example = "2025-12-31 23:59:59", required = true)
    private String endTime;

    /**
     * 优惠券描述
     */
    @Schema(description = "优惠券描述", example = "新用户注册专享，满50元减10元")
    private String description;
}
