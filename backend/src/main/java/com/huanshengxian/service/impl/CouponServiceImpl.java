package com.huanshengxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huanshengxian.dto.CouponCreateDTO;
import com.huanshengxian.entity.Coupon;
import com.huanshengxian.entity.UserCoupon;
import com.huanshengxian.mapper.CouponMapper;
import com.huanshengxian.mapper.UserCouponMapper;
import com.huanshengxian.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 优惠券服务实现类
 * 实现优惠券的业务逻辑，包括校验、领取、使用等核心功能
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    public Coupon createCoupon(Coupon coupon) {
        validateCouponParams(coupon);
        coupon.setReceivedCount(0);
        coupon.setUsedCount(0);
        if (coupon.getStatus() == null) {
            coupon.setStatus(Coupon.STATUS_ENABLED);
        }
        this.save(coupon);
        return coupon;
    }

    @Override
    public Coupon createCouponFromDTO(CouponCreateDTO dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Coupon coupon = Coupon.builder()
                .name(dto.getName())
                .type(dto.getType())
                .discountValue(dto.getDiscountValue())
                .minAmount(dto.getMinAmount() != null ? dto.getMinAmount() : BigDecimal.ZERO)
                .totalCount(dto.getTotalCount())
                .startTime(LocalDateTime.parse(dto.getStartTime(), formatter))
                .endTime(LocalDateTime.parse(dto.getEndTime(), formatter))
                .description(dto.getDescription())
                .build();
        return createCoupon(coupon);
    }

    @Override
    public Page<Coupon> listCoupons(Integer page, Integer size, String type, Integer status) {
        Page<Coupon> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq(Coupon::getType, type);
        }
        if (status != null) {
            wrapper.eq(Coupon::getStatus, status);
        }
        wrapper.orderByDesc(Coupon::getCreatedAt);
        return this.page(pageParam, wrapper);
    }

    @Override
    public void updateCouponStatus(Long couponId, Integer status) {
        Coupon coupon = this.getById(couponId);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        coupon.setStatus(status);
        this.updateById(coupon);
    }

    @Override
    public Map<String, Object> getCouponStats(Long couponId) {
        Map<String, Object> stats = couponMapper.selectCouponStats(couponId);
        if (stats == null) {
            throw new RuntimeException("优惠券不存在");
        }
        return stats;
    }

    @Override
    public List<Coupon> getAvailableCoupons(Long userId) {
        return couponMapper.selectAvailableCoupons(userId);
    }

    @Override
    @Transactional
    public UserCoupon receiveCoupon(Long userId, Long couponId) {
        Coupon coupon = this.getById(couponId);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        if (!coupon.isValidPeriod()) {
            throw new RuntimeException("优惠券不在有效期内");
        }

        if (coupon.getStatus() != Coupon.STATUS_ENABLED) {
            throw new RuntimeException("优惠券已禁用");
        }

        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new RuntimeException("优惠券已发完");
        }

        Integer receivedCount = userCouponMapper.countByUserAndCoupon(userId, couponId);
        if (receivedCount != null && receivedCount > 0) {
            throw new RuntimeException("您已领取过该优惠券");
        }

        coupon.setReceivedCount(coupon.getReceivedCount() + 1);
        this.updateById(coupon);

        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .status(UserCoupon.STATUS_UNUSED)
                .receivedAt(LocalDateTime.now())
                .build();
        userCouponMapper.insert(userCoupon);

        return userCoupon;
    }

    @Override
    public List<UserCoupon> getMyCoupons(Long userId, Integer status) {
        return userCouponMapper.selectByUserIdAndStatus(userId, status);
    }

    @Override
    public List<Coupon> getUsableCoupons(Long userId, BigDecimal orderAmount) {
        return couponMapper.selectUsableCoupons(userId, orderAmount);
    }

    @Override
    public BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new RuntimeException("用户优惠券不存在");
        }
        if (userCoupon.getStatus() != UserCoupon.STATUS_UNUSED) {
            throw new RuntimeException("优惠券已使用或已过期");
        }
        Coupon coupon = this.getById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        if (!coupon.isValidPeriod()) {
            throw new RuntimeException("优惠券已过期");
        }
        return coupon.calculateDiscount(orderAmount);
    }

    @Override
    @Transactional
    public void useCoupon(Long userCouponId, Long orderId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new RuntimeException("用户优惠券不存在");
        }

        if (userCoupon.getStatus() != UserCoupon.STATUS_UNUSED) {
            throw new RuntimeException("优惠券已使用或已过期");
        }

        Coupon coupon = this.getById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        if (!coupon.isValidPeriod()) {
            throw new RuntimeException("优惠券已过期");
        }

        userCoupon.setStatus(UserCoupon.STATUS_USED);
        userCoupon.setOrderId(orderId);
        userCoupon.setUsedAt(LocalDateTime.now());
        userCouponMapper.updateById(userCoupon);

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        this.updateById(coupon);
    }

    @Override
    @Transactional
    public void rollbackCoupon(Long userCouponId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            return;
        }

        if (userCoupon.getStatus() != UserCoupon.STATUS_USED) {
            return;
        }

        userCoupon.setStatus(UserCoupon.STATUS_UNUSED);
        userCoupon.setOrderId(null);
        userCoupon.setUsedAt(null);
        userCouponMapper.updateById(userCoupon);

        Coupon coupon = this.getById(userCoupon.getCouponId());
        if (coupon != null) {
            coupon.setUsedCount(Math.max(0, coupon.getUsedCount() - 1));
            this.updateById(coupon);
        }
    }

    @Override
    @Transactional
    public void expireCoupons() {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getStatus, UserCoupon.STATUS_UNUSED);
        List<UserCoupon> unusedCoupons = userCouponMapper.selectList(wrapper);

        LocalDateTime now = LocalDateTime.now();
        for (UserCoupon userCoupon : unusedCoupons) {
            Coupon coupon = this.getById(userCoupon.getCouponId());
            if (coupon != null && now.isAfter(coupon.getEndTime())) {
                userCoupon.setStatus(UserCoupon.STATUS_EXPIRED);
                userCouponMapper.updateById(userCoupon);
            }
        }
    }

    /**
     * 校验优惠券参数合法性
     * @param coupon 优惠券信息
     */
    private void validateCouponParams(Coupon coupon) {
        if (!StringUtils.hasText(coupon.getName())) {
            throw new RuntimeException("优惠券名称不能为空");
        }
        if (!StringUtils.hasText(coupon.getType())) {
            throw new RuntimeException("优惠券类型不能为空");
        }
        if (!Coupon.TYPE_FULL_REDUCTION.equals(coupon.getType())
                && !Coupon.TYPE_DISCOUNT.equals(coupon.getType())
                && !Coupon.TYPE_NO_THRESHOLD.equals(coupon.getType())) {
            throw new RuntimeException("优惠券类型不合法");
        }
        if (coupon.getDiscountValue() == null || coupon.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("优惠面额/折扣值必须大于0");
        }
        if (Coupon.TYPE_DISCOUNT.equals(coupon.getType()) && coupon.getDiscountValue().compareTo(BigDecimal.ONE) < 0) {
            throw new RuntimeException("折扣券折扣值不能小于1折");
        }
        if (Coupon.TYPE_DISCOUNT.equals(coupon.getType()) && coupon.getDiscountValue().compareTo(new BigDecimal("9.9")) > 0) {
            throw new RuntimeException("折扣券折扣值不能大于9.9折");
        }
        if (Coupon.TYPE_FULL_REDUCTION.equals(coupon.getType()) && (coupon.getMinAmount() == null || coupon.getMinAmount().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new RuntimeException("满减券必须设置使用门槛金额");
        }
        if (Coupon.TYPE_FULL_REDUCTION.equals(coupon.getType()) && coupon.getMinAmount().compareTo(coupon.getDiscountValue()) <= 0) {
            throw new RuntimeException("满减券的使用门槛金额必须大于优惠面额");
        }
        if (coupon.getTotalCount() == null || coupon.getTotalCount() <= 0) {
            throw new RuntimeException("发放总量必须大于0");
        }
        if (coupon.getStartTime() == null || coupon.getEndTime() == null) {
            throw new RuntimeException("有效期不能为空");
        }
        if (coupon.getEndTime().isBefore(coupon.getStartTime())) {
            throw new RuntimeException("有效期结束时间不能早于开始时间");
        }
    }
}
