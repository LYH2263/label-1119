package com.huanshengxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huanshengxian.entity.Feedback;
import com.huanshengxian.entity.Product;
import com.huanshengxian.entity.SalesAnalytics;
import com.huanshengxian.entity.Supplier;
import com.huanshengxian.mapper.FeedbackMapper;
import com.huanshengxian.mapper.ProductMapper;
import com.huanshengxian.mapper.SalesAnalyticsMapper;
import com.huanshengxian.mapper.SupplierMapper;
import com.huanshengxian.service.SupplierPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 供应商门户服务实现
 */
@Service
@RequiredArgsConstructor
public class SupplierPortalServiceImpl implements SupplierPortalService {

    private final SupplierMapper supplierMapper;
    private final ProductMapper productMapper;
    private final FeedbackMapper feedbackMapper;
    private final SalesAnalyticsMapper salesAnalyticsMapper;

    @Override
    public Supplier getSupplierByUserId(Long userId) {
        Supplier supplier = supplierMapper.selectOne(new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getUserId, userId)
                .eq(Supplier::getStatus, 1)
                .last("LIMIT 1"));
        if (supplier == null) {
            throw new RuntimeException("当前账号不是供应商账号");
        }
        return supplier;
    }

    @Override
    public Supplier updateSupplierProfile(Long userId, Map<String, Object> params) {
        Supplier supplier = getSupplierByUserId(userId);

        if (params.get("contactPerson") != null) {
            supplier.setContactPerson(String.valueOf(params.get("contactPerson")));
        }
        if (params.get("contactPhone") != null) {
            supplier.setContactPhone(String.valueOf(params.get("contactPhone")));
        }
        if (params.get("address") != null) {
            supplier.setAddress(String.valueOf(params.get("address")));
        }
        if (params.get("description") != null) {
            supplier.setDescription(String.valueOf(params.get("description")));
        }

        supplierMapper.updateById(supplier);
        return supplier;
    }

    @Override
    public Map<String, Object> getSupplierDashboard(Long userId) {
        Supplier supplier = getSupplierByUserId(userId);
        List<Product> products = getSupplierProducts(supplier.getId());
        List<Feedback> feedbacks = getFeedbacksByProducts(products);
        List<SalesAnalytics> analytics = getAnalyticsBySupplier(supplier.getId());

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("supplier", supplier);
        dashboard.put("products", products);
        dashboard.put("feedbacks", feedbacks.stream().limit(10).toList());
        dashboard.put("feedbackStats", buildFeedbackStats(feedbacks));
        dashboard.put("salesStats", buildSalesStats(analytics, products));
        dashboard.put("recommendations", buildRecommendations(products, feedbacks, analytics));
        dashboard.put("syncedAt", analytics.stream()
                .map(SalesAnalytics::getDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .map(LocalDate::toString)
                .orElse(LocalDate.now().toString()));
        return dashboard;
    }

    @Override
    public List<Feedback> getSupplierFeedbacks(Long userId) {
        Supplier supplier = getSupplierByUserId(userId);
        return getFeedbacksByProducts(getSupplierProducts(supplier.getId()));
    }

    @Override
    public Feedback replyFeedback(Long userId, Long feedbackId, String reply) {
        if (!StringUtils.hasText(reply)) {
            throw new RuntimeException("回复内容不能为空");
        }

        Supplier supplier = getSupplierByUserId(userId);
        Feedback feedback = feedbackMapper.selectById(feedbackId);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }
        if (feedback.getProductId() == null) {
            throw new RuntimeException("该反馈未关联商品，无法由供应商处理");
        }

        Product product = productMapper.selectById(feedback.getProductId());
        if (product == null || !supplier.getId().equals(product.getSupplierId())) {
            throw new RuntimeException("无权回复该反馈");
        }

        feedback.setReply(reply.trim());
        feedback.setStatus(1);
        feedbackMapper.updateById(feedback);
        return feedback;
    }

    @Override
    public List<Map<String, Object>> getSupplierInsights() {
        List<Supplier> suppliers = supplierMapper.selectList(new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getStatus, 1)
                .orderByAsc(Supplier::getId));

        return suppliers.stream().map(supplier -> {
            List<Product> products = getSupplierProducts(supplier.getId());
            List<Feedback> feedbacks = getFeedbacksByProducts(products);
            List<SalesAnalytics> analytics = getAnalyticsBySupplier(supplier.getId());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("supplierId", supplier.getId());
            item.put("supplierName", supplier.getName());
            item.put("contactPerson", supplier.getContactPerson());
            item.put("address", supplier.getAddress());
            item.put("productCount", products.size());
            item.put("hotProducts", products.stream()
                    .sorted(Comparator.comparing(Product::getSales, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(3)
                    .map(Product::getName)
                    .toList());

            Map<String, Object> feedbackStats = buildFeedbackStats(feedbacks);
            Map<String, Object> salesStats = buildSalesStats(analytics, products);
            item.put("feedbackStats", feedbackStats);
            item.put("salesStats", salesStats);
            item.put("recommendations", buildRecommendations(products, feedbacks, analytics));
            item.put("syncedAt", analytics.stream()
                    .map(SalesAnalytics::getDate)
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .map(LocalDate::toString)
                    .orElse(LocalDate.now().toString()));
            return item;
        }).toList();
    }

    private List<Product> getSupplierProducts(Long supplierId) {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getSupplierId, supplierId)
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales));
    }

    private List<Feedback> getFeedbacksByProducts(List<Product> products) {
        if (products.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = products.stream().map(Product::getId).toList();
        List<Feedback> feedbacks = feedbackMapper.selectList(new LambdaQueryWrapper<Feedback>()
                .in(Feedback::getProductId, productIds)
                .orderByDesc(Feedback::getCreatedAt));

        Map<Long, String> productNameMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Product::getName));
        feedbacks.forEach(feedback -> feedback.setProductName(productNameMap.get(feedback.getProductId())));
        return feedbacks;
    }

    private List<SalesAnalytics> getAnalyticsBySupplier(Long supplierId) {
        return salesAnalyticsMapper.selectList(new LambdaQueryWrapper<SalesAnalytics>()
                .eq(SalesAnalytics::getSupplierId, supplierId)
                .orderByDesc(SalesAnalytics::getDate));
    }

    private Map<String, Object> buildFeedbackStats(List<Feedback> feedbacks) {
        Map<String, Object> stats = new LinkedHashMap<>();
        long pendingCount = feedbacks.stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == 0)
                .count();
        double avgRating = feedbacks.stream()
                .filter(item -> item.getRating() != null)
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        Map<Integer, Long> ratingStats = feedbacks.stream()
                .filter(item -> item.getRating() != null)
                .collect(Collectors.groupingBy(Feedback::getRating, LinkedHashMap::new, Collectors.counting()));

        stats.put("totalFeedback", feedbacks.size());
        stats.put("pendingFeedback", pendingCount);
        stats.put("avgRating", Math.round(avgRating * 10) / 10.0);
        stats.put("ratingStats", ratingStats);
        return stats;
    }

    private Map<String, Object> buildSalesStats(List<SalesAnalytics> analytics, List<Product> products) {
        Map<String, Object> stats = new LinkedHashMap<>();
        BigDecimal totalSalesAmount = analytics.stream()
                .map(SalesAnalytics::getSalesAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalSalesCount = analytics.stream()
                .map(SalesAnalytics::getSalesCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        int totalOrderCount = analytics.stream()
                .map(SalesAnalytics::getOrderCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        String topProduct = products.stream()
                .max(Comparator.comparing(Product::getSales, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(Product::getName)
                .orElse("暂无");

        stats.put("totalSalesAmount", totalSalesAmount);
        stats.put("totalSalesCount", totalSalesCount);
        stats.put("totalOrderCount", totalOrderCount);
        stats.put("topProduct", topProduct);
        return stats;
    }

    private List<String> buildRecommendations(List<Product> products, List<Feedback> feedbacks, List<SalesAnalytics> analytics) {
        List<String> recommendations = new ArrayList<>();

        double avgRating = feedbacks.stream()
                .filter(item -> item.getRating() != null)
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);
        if (!feedbacks.isEmpty() && avgRating < 4.5) {
            recommendations.add("近期用户评分偏低，建议优先复盘采摘分级、保鲜包装和到货口感。");
        }

        Map<Long, Long> lowRatingCounts = feedbacks.stream()
                .filter(item -> item.getRating() != null && item.getRating() <= 3 && item.getProductId() != null)
                .collect(Collectors.groupingBy(Feedback::getProductId, Collectors.counting()));
        if (!lowRatingCounts.isEmpty()) {
            Long productId = lowRatingCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
            String productName = products.stream()
                    .filter(item -> Objects.equals(item.getId(), productId))
                    .map(Product::getName)
                    .findFirst()
                    .orElse("当前商品");
            recommendations.add(productName + " 的低分反馈较多，建议针对口感、规格稳定性和包装进行专项改进。");
        }

        Product topProduct = products.stream()
                .max(Comparator.comparing(Product::getSales, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
        if (topProduct != null && topProduct.getSales() != null && topProduct.getSales() > 1000) {
            recommendations.add(topProduct.getName() + " 销量表现持续领先，可适度扩大种植面积并优先保障优质批次供应。");
        }

        long pendingFeedback = feedbacks.stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == 0)
                .count();
        if (pendingFeedback > 0) {
            recommendations.add("当前仍有 " + pendingFeedback + " 条消费者反馈待处理，建议及时回复并跟进种植改进计划。");
        }

        if (recommendations.isEmpty()) {
            BigDecimal totalSalesAmount = analytics.stream()
                    .map(SalesAnalytics::getSalesAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            recommendations.add("近期经营数据整体稳定，建议继续保持当前种植节奏，并按周复盘销量与评分变化。");
            if (totalSalesAmount.compareTo(new BigDecimal("1000")) > 0) {
                recommendations.add("销售额保持增长，可结合历史销量提前安排下一周期播种与采收计划。");
            }
        }

        return recommendations.stream().limit(4).toList();
    }
}
