package com.huanshengxian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 优惠券实体类
 * 用于存储优惠券的基本信息，包括类型、面额、使用门槛、有效期等
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("coupons")
public class Coupon {

    /**
     * 优惠券类型常量: 满减券
     */
    public static final String TYPE_FULL_REDUCTION = "FULL_REDUCTION";

    /**
     * 优惠券类型常量: 折扣券
     */
    public static final String TYPE_DISCOUNT = "DISCOUNT";

    /**
     * 优惠券类型常量: 无门槛券
     */
    public static final String TYPE_NO_THRESHOLD = "NO_THRESHOLD";

    /**
     * 优惠券状态常量: 禁用
     */
    public static final int STATUS_DISABLED = 0;

    /**
     * 优惠券状态常量: 启用
     */
    public static final int STATUS_ENABLED = 1;

    /**
     * 优惠券ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 优惠券类型: FULL_REDUCTION-满减券, DISCOUNT-折扣券, NO_THRESHOLD-无门槛券
     */
    private String type;

    /**
     * 面额/折扣值: 满减券为减免金额, 折扣券为折扣率(如8.5表示85折), 无门槛券为减免金额
     */
    private BigDecimal discountValue;

    /**
     * 使用门槛金额: 满减券需要满足的最低订单金额, 无门槛券和折扣券为0
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
     * 有效期开始时间
     */
    private LocalDateTime startTime;

    /**
     * 有效期结束时间
     */
    private LocalDateTime endTime;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 优惠券描述
     */
    private String description;

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
     * 非数据库字段: 当前用户是否已领取该优惠券
     */
    @TableField(exist = false)
    private Boolean received;

    /**
     * 计算优惠金额
     * @param orderAmount 订单金额
     * @return 优惠金额
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (TYPE_FULL_REDUCTION.equals(this.type)) {
            if (orderAmount.compareTo(this.minAmount) >= 0) {
                return this.discountValue;
            }
            return BigDecimal.ZERO;
        } else if (TYPE_DISCOUNT.equals(this.type)) {
            if (orderAmount.compareTo(this.minAmount) >= 0) {
                BigDecimal discountMultiplier = this.discountValue.divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
                BigDecimal payAmount = orderAmount.multiply(discountMultiplier);
                return orderAmount.subtract(payAmount);
            }
            return BigDecimal.ZERO;
        } else if (TYPE_NO_THRESHOLD.equals(this.type)) {
            return this.discountValue;
        }
        return BigDecimal.ZERO;
    }

    /**
     * 判断优惠券是否在有效期内
     * @return 是否有效
     */
    public boolean isValidPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(this.startTime) && now.isBefore(this.endTime);
    }
}
