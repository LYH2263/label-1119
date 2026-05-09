package com.huanshengxian.constant;

/**
 * 优惠券常量类
 * 功能说明：定义优惠券相关的类型、状态常量
 */
public class CouponConstants {

    /**
     * 优惠券类型：满减券
     */
    public static final String TYPE_FULL_REDUCTION = "FULL_REDUCTION";

    /**
     * 优惠券类型：折扣券
     */
    public static final String TYPE_DISCOUNT = "DISCOUNT";

    /**
     * 优惠券类型：无门槛券
     */
    public static final String TYPE_NO_THRESHOLD = "NO_THRESHOLD";

    /**
     * 用户优惠券状态：未使用
     */
    public static final String USER_COUPON_STATUS_UNUSED = "UNUSED";

    /**
     * 用户优惠券状态：已使用
     */
    public static final String USER_COUPON_STATUS_USED = "USED";

    /**
     * 用户优惠券状态：已过期
     */
    public static final String USER_COUPON_STATUS_EXPIRED = "EXPIRED";

    /**
     * 优惠券状态：启用
     */
    public static final Integer COUPON_STATUS_ENABLED = 1;

    /**
     * 优惠券状态：禁用
     */
    public static final Integer COUPON_STATUS_DISABLED = 0;

    private CouponConstants() {
    }
}
