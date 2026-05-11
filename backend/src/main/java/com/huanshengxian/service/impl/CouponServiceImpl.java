package com.huanshengxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huanshengxian.entity.Coupon;
import com.huanshengxian.entity.UserCoupon;
import com.huanshengxian.mapper.CouponMapper;
import com.huanshengxian.mapper.UserCouponMapper;
import com.huanshengxian.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券服务实现
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon createCoupon(Coupon coupon) {
        coupon.setReceivedCount(0);
        coupon.setUsedCount(0);
        coupon.setStatus(Coupon.STATUS_ENABLED);
        couponMapper.insert(coupon);
        return coupon;
    }

    @Override
    public Page<Coupon> pageCoupons(Integer page, Integer size, String name, Integer type, Integer status) {
        Page<Coupon> pageParam = new Page<>(page, size);
        return couponMapper.selectCouponPage(pageParam, name, type, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        Coupon coupon = new Coupon();
        coupon.setId(id);
        coupon.setStatus(status);
        return couponMapper.updateById(coupon) > 0;
    }

    @Override
    public Map<String, Object> getCouponStats(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", coupon.getTotalCount());
        stats.put("receivedCount", coupon.getReceivedCount());
        stats.put("usedCount", coupon.getUsedCount());
        stats.put("remainingCount", coupon.getTotalCount() - coupon.getReceivedCount());
        stats.put("receiveRate", coupon.getTotalCount() > 0
                ? BigDecimal.valueOf(coupon.getReceivedCount())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(coupon.getTotalCount()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        stats.put("useRate", coupon.getReceivedCount() > 0
                ? BigDecimal.valueOf(coupon.getUsedCount())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(coupon.getReceivedCount()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        return stats;
    }

    @Override
    public List<Coupon> getAvailableCoupons(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return couponMapper.selectAvailableCoupons(userId, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCoupon claimCoupon(Long userId, Long couponId) {
        LocalDateTime now = LocalDateTime.now();
        
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        
        if (!coupon.getStatus().equals(Coupon.STATUS_ENABLED)) {
            throw new RuntimeException("优惠券已禁用");
        }
        
        if (now.isBefore(coupon.getValidStartTime()) || now.isAfter(coupon.getValidEndTime())) {
            throw new RuntimeException("优惠券不在有效期内");
        }
        
        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new RuntimeException("优惠券已被领完");
        }
        
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
               .eq(UserCoupon::getCouponId, couponId);
        Long count = userCouponMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("您已领取过该优惠券");
        }
        
        int updated = couponMapper.incrementReceivedCount(couponId);
        if (updated <= 0) {
            throw new RuntimeException("领取失败，请稍后重试");
        }
        
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .status(UserCoupon.STATUS_UNUSED)
                .receivedTime(now)
                .build();
        userCouponMapper.insert(userCoupon);
        
        return userCoupon;
    }

    @Override
    public Page<UserCoupon> pageUserCoupons(Long userId, Integer page, Integer size, Integer status) {
        Page<UserCoupon> pageParam = new Page<>(page, size);
        return userCouponMapper.selectUserCouponPage(pageParam, userId, status);
    }

    @Override
    public List<UserCoupon> getUserAvailableCoupons(Long userId, BigDecimal orderAmount) {
        LocalDateTime now = LocalDateTime.now();
        return userCouponMapper.selectAvailableCouponsWithDetail(userId, orderAmount, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal useCoupon(Long userCouponId, Long userId, Long orderId, BigDecimal orderAmount) {
        LocalDateTime now = LocalDateTime.now();
        
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        
        if (!userCoupon.getUserId().equals(userId)) {
            throw new RuntimeException("该优惠券不属于当前用户");
        }
        
        if (!userCoupon.getStatus().equals(UserCoupon.STATUS_UNUSED)) {
            throw new RuntimeException("优惠券状态异常");
        }
        
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new RuntimeException("优惠券信息不存在");
        }
        
        if (now.isBefore(coupon.getValidStartTime()) || now.isAfter(coupon.getValidEndTime())) {
            throw new RuntimeException("优惠券已过期");
        }
        
        if (orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new RuntimeException("订单金额不满足使用门槛");
        }
        
        BigDecimal discountAmount = calculateDiscountAmount(coupon, orderAmount);
        
        userCoupon.setStatus(UserCoupon.STATUS_USED);
        userCoupon.setOrderId(orderId);
        userCoupon.setUsedTime(now);
        userCouponMapper.updateById(userCoupon);
        
        couponMapper.incrementUsedCount(coupon.getId());
        
        return discountAmount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackCoupon(Long orderId) {
        UserCoupon userCoupon = userCouponMapper.selectByOrderId(orderId);
        if (userCoupon == null) {
            return true;
        }
        
        userCoupon.setStatus(UserCoupon.STATUS_UNUSED);
        userCoupon.setOrderId(null);
        userCoupon.setUsedTime(null);
        int updated = userCouponMapper.updateById(userCoupon);
        
        if (updated > 0) {
            couponMapper.decrementUsedCount(userCoupon.getCouponId());
        }
        
        return updated > 0;
    }

    @Override
    public BigDecimal calculateDiscountAmount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        switch (coupon.getType()) {
            case Coupon.TYPE_FULL_REDUCTION:
            case Coupon.TYPE_NO_THRESHOLD:
                discountAmount = coupon.getAmount();
                break;
            case Coupon.TYPE_DISCOUNT:
                discountAmount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscount()))
                        .setScale(2, RoundingMode.HALF_UP);
                break;
            default:
                break;
        }
        
        if (discountAmount.compareTo(orderAmount) > 0) {
            discountAmount = orderAmount;
        }
        
        return discountAmount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateExpiredCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return userCouponMapper.updateExpiredCoupons(now);
    }
}
