package com.huanshengxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huanshengxian.constant.CouponConstants;
import com.huanshengxian.dto.CouponCalculateDTO;
import com.huanshengxian.dto.CouponCreateDTO;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 优惠券服务实现类
 * 功能说明：
 * - 实现优惠券的创建、查询、发放、领取、使用等核心业务逻辑
 * - 包含严格的业务校验：领取校验、使用校验、事务回滚
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon createCoupon(CouponCreateDTO dto) {
        Coupon coupon = Coupon.builder()
                .name(dto.getName())
                .type(dto.getType())
                .value(dto.getValue())
                .minAmount(dto.getMinAmount())
                .totalCount(dto.getTotalCount())
                .receivedCount(0)
                .usedCount(0)
                .status(CouponConstants.COUPON_STATUS_ENABLED)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .build();
        couponMapper.insert(coupon);
        return coupon;
    }

    @Override
    public Page<Coupon> getCouponPage(Integer page, Integer size, String name, String type) {
        Page<Coupon> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Coupon::getName, name);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Coupon::getType, type);
        }
        wrapper.orderByDesc(Coupon::getCreatedAt);
        return couponMapper.selectPage(pageParam, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        coupon.setStatus(status);
        couponMapper.updateById(coupon);
    }

    @Override
    public Coupon getCouponStats(Long id) {
        return couponMapper.selectById(id);
    }

    @Override
    public List<Coupon> getAvailableCoupons() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getStatus, CouponConstants.COUPON_STATUS_ENABLED)
                .apply("received_count < total_count")
                .le(Coupon::getStartTime, now)
                .ge(Coupon::getEndTime, now)
                .orderByDesc(Coupon::getCreatedAt);
        return couponMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCoupon claimCoupon(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        if (coupon.getStatus() != CouponConstants.COUPON_STATUS_ENABLED) {
            throw new RuntimeException("优惠券已禁用");
        }

        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new RuntimeException("优惠券已领完");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new RuntimeException("优惠券不在有效期内");
        }

        LambdaQueryWrapper<UserCoupon> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId);
        if (userCouponMapper.selectCount(existWrapper) > 0) {
            throw new RuntimeException("您已领取过该优惠券");
        }

        coupon.setReceivedCount(coupon.getReceivedCount() + 1);
        couponMapper.updateById(coupon);

        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponId(couponId)
                .status(CouponConstants.USER_COUPON_STATUS_UNUSED)
                .receivedAt(now)
                .build();
        userCouponMapper.insert(userCoupon);

        return userCoupon;
    }

    @Override
    public Page<UserCoupon> getMyCoupons(Long userId, Integer page, Integer size, String status) {
        Page<UserCoupon> pageParam = new Page<>(page, size);
        List<UserCoupon> list = userCouponMapper.selectUserCouponsWithCoupon(pageParam, userId, status);
        pageParam.setRecords(list);
        return pageParam;
    }

    @Override
    public BigDecimal calculateDiscount(Long userId, CouponCalculateDTO dto) {
        UserCoupon userCoupon = userCouponMapper.selectById(dto.getUserCouponId());
        if (userCoupon == null) {
            throw new RuntimeException("用户优惠券不存在");
        }
        if (!userCoupon.getUserId().equals(userId)) {
            throw new RuntimeException("无权使用该优惠券");
        }

        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        validateCouponUsable(userCoupon, coupon, dto.getOrderAmount());

        return calculateDiscountAmount(coupon, dto.getOrderAmount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(Long userId, Long userCouponId, Long orderId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new RuntimeException("用户优惠券不存在");
        }
        if (!userCoupon.getUserId().equals(userId)) {
            throw new RuntimeException("无权使用该优惠券");
        }
        if (!CouponConstants.USER_COUPON_STATUS_UNUSED.equals(userCoupon.getStatus())) {
            throw new RuntimeException("优惠券状态异常");
        }

        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }

        userCoupon.setStatus(CouponConstants.USER_COUPON_STATUS_USED);
        userCoupon.setOrderId(orderId);
        userCoupon.setUsedAt(LocalDateTime.now());
        userCouponMapper.updateById(userCoupon);

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponMapper.updateById(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackCoupon(Long userCouponId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon != null && CouponConstants.USER_COUPON_STATUS_USED.equals(userCoupon.getStatus())) {
            userCoupon.setStatus(CouponConstants.USER_COUPON_STATUS_UNUSED);
            userCoupon.setOrderId(null);
            userCoupon.setUsedAt(null);
            userCouponMapper.updateById(userCoupon);

            Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
            if (coupon != null && coupon.getUsedCount() > 0) {
                coupon.setUsedCount(coupon.getUsedCount() - 1);
                couponMapper.updateById(coupon);
            }
        }
    }

    @Override
    public List<UserCoupon> getAvailableForOrder(Long userId, BigDecimal orderAmount) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, CouponConstants.USER_COUPON_STATUS_UNUSED)
                .orderByDesc(UserCoupon::getReceivedAt);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(wrapper);

        List<UserCoupon> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (UserCoupon uc : userCoupons) {
            Coupon coupon = couponMapper.selectById(uc.getCouponId());
            if (coupon != null) {
                boolean inTime = !now.isBefore(coupon.getStartTime()) && !now.isAfter(coupon.getEndTime());
                boolean meetsMin = orderAmount.compareTo(coupon.getMinAmount()) >= 0;
                if (inTime && meetsMin) {
                    uc.setCoupon(coupon);
                    result.add(uc);
                }
            }
        }
        return result;
    }

    /**
     * 校验优惠券是否可用
     *
     * @param userCoupon 用户优惠券
     * @param coupon     优惠券详情
     * @param orderAmount 订单金额
     */
    private void validateCouponUsable(UserCoupon userCoupon, Coupon coupon, BigDecimal orderAmount) {
        if (!CouponConstants.USER_COUPON_STATUS_UNUSED.equals(userCoupon.getStatus())) {
            throw new RuntimeException("优惠券不可用");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new RuntimeException("优惠券已过期");
        }

        if (orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new RuntimeException("订单金额不满足使用门槛");
        }
    }

    /**
     * 计算优惠金额
     *
     * @param coupon      优惠券
     * @param orderAmount 订单金额
     * @return 优惠金额
     */
    private BigDecimal calculateDiscountAmount(Coupon coupon, BigDecimal orderAmount) {
        String type = coupon.getType();
        if (CouponConstants.TYPE_FULL_REDUCTION.equals(type)) {
            return coupon.getValue();
        } else if (CouponConstants.TYPE_DISCOUNT.equals(type)) {
            BigDecimal discount = orderAmount.multiply(coupon.getValue());
            return orderAmount.subtract(discount).setScale(2, RoundingMode.HALF_UP);
        } else if (CouponConstants.TYPE_NO_THRESHOLD.equals(type)) {
            return coupon.getValue();
        } else {
            throw new RuntimeException("未知的优惠券类型");
        }
    }
}
