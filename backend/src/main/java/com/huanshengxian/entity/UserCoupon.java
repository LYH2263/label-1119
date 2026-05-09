package com.huanshengxian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户优惠券关联实体类
 * 功能说明：
 * - 记录用户领取的优惠券
 * - 包含领取时间、使用时间、状态等属性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_coupons")
public class UserCoupon {

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
     * 状态：UNUSED-未使用、USED-已使用、EXPIRED-已过期
     */
    private String status;

    /**
     * 订单ID（使用时关联）
     */
    private Long orderId;

    /**
     * 领取时间
     */
    private LocalDateTime receivedAt;

    /**
     * 使用时间
     */
    private LocalDateTime usedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private LocalDateTime deletedAt;

    @TableField(exist = false)
    private Coupon coupon;
}
