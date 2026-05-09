package com.huanshengxian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体类
 * 功能说明：
 * - 支持三种类型：满减券、折扣券、无门槛券
 * - 包含名称、类型、面额、使用门槛、有效期、发放总量等属性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("coupons")
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 优惠券类型：FULL_REDUCTION-满减券、DISCOUNT-折扣券、NO_THRESHOLD-无门槛券
     */
    private String type;

    /**
     * 面额/折扣值
     * 满减券：满减金额
     * 折扣券：折扣率（如0.8表示8折）
     * 无门槛券：减免金额
     */
    private BigDecimal value;

    /**
     * 使用门槛（最低消费金额）
     */
    private BigDecimal minAmount;

    /**
     * 发放总量
     */
    private Integer totalCount;

    /**
     * 已领取数量
     */
    private Integer receivedCount;

    /**
     * 已使用数量
     */
    private Integer usedCount;

    /**
     * 状态：1-启用、0-禁用
     */
    private Integer status;

    /**
     * 有效期开始时间
     */
    private LocalDateTime startTime;

    /**
     * 有效期结束时间
     */
    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private LocalDateTime deletedAt;
}
