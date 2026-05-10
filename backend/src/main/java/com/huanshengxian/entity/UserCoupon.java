package com.huanshengxian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券关联实体类
 * 用于记录用户领取优惠券的信息和使用状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_coupons")
public class UserCoupon {

    /**
     * 用户优惠券状态常量: 未使用
     */
    public static final int STATUS_UNUSED = 0;

    /**
     * 用户优惠券状态常量: 已使用
     */
    public static final int STATUS_USED = 1;

    /**
     * 用户优惠券状态常量: 已过期
     */
    public static final int STATUS_EXPIRED = 2;

    /**
     * 用户优惠券ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 使用该优惠券的订单ID
     */
    private Long orderId;

    /**
     * 状态: 0-未使用, 1-已使用, 2-已过期
     */
    private Integer status;

    /**
     * 领取时间
     */
    private LocalDateTime receivedAt;

    /**
     * 使用时间
     */
    private LocalDateTime usedAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 软删除时间
     */
    @TableLogic(value = "", delval = "NOW()")
    @TableField(select = false)
    private LocalDateTime deletedAt;

    /**
     * 非数据库字段: 关联的优惠券信息
     */
    @TableField(exist = false)
    private Coupon coupon;

    /**
     * 非数据库字段: 计算出的优惠金额
     */
    @TableField(exist = false)
    private BigDecimal discountAmount;
}
