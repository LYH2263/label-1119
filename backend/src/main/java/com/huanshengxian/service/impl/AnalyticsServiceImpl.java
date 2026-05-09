package com.huanshengxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huanshengxian.entity.*;
import com.huanshengxian.mapper.*;
import com.huanshengxian.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据分析服务实现
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final FeedbackMapper feedbackMapper;
    private final UserMapper userMapper;
    private final SalesAnalyticsMapper salesAnalyticsMapper;

    @Override
    public Map<String, Object> getSalesOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 总销售额
        List<Order> completedOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .in(Order::getStatus, "PAID", "SHIPPED", "DELIVERED", "COMPLETED"));
        BigDecimal totalSales = completedOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        overview.put("totalSales", totalSales);
        
        // 总订单数
        overview.put("totalOrders", completedOrders.size());
        
        // 总商品数
        long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1));
        overview.put("totalProducts", productCount);
        
        // 总用户数
        long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1));
        overview.put("totalUsers", userCount);
        
        // 平均订单金额
        if (!completedOrders.isEmpty()) {
            overview.put("avgOrderAmount", totalSales.divide(new BigDecimal(completedOrders.size()), 2, BigDecimal.ROUND_HALF_UP));
        } else {
            overview.put("avgOrderAmount", BigDecimal.ZERO);
        }
        
        return overview;
    }

    @Override
    public List<Map<String, Object>> getHotProductsRanking(Integer limit) {
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales)
                .last("LIMIT " + limit));
        
        return products.stream().map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("name", p.getName());
            item.put("sales", p.getSales());
            item.put("price", p.getPrice());
            item.put("images", p.getImages());
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getCategorySales() {
        List<Category> topCategories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .eq(Category::getParentId, 0));
        
        return topCategories.stream().map(cat -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", cat.getId());
            item.put("name", cat.getName());
            item.put("icon", cat.getIcon());
            
            // 统计该分类下的销售量
            List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                    .eq(Product::getCategoryId, cat.getId())
                    .eq(Product::getStatus, 1));
            int totalSales = products.stream().mapToInt(Product::getSales).sum();
            item.put("sales", totalSales);
            
            return item;
        }).sorted((a, b) -> (int) b.get("sales") - (int) a.get("sales"))
          .collect(Collectors.toList());
    }

    @Override
    public List<SalesAnalytics> getSalesTrend(LocalDate startDate, LocalDate endDate) {
        return salesAnalyticsMapper.selectList(new LambdaQueryWrapper<SalesAnalytics>()
                .between(SalesAnalytics::getDate, startDate, endDate)
                .orderByAsc(SalesAnalytics::getDate));
    }

    @Override
    public Map<String, Object> getFeedbackStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总反馈数
        long totalFeedback = feedbackMapper.selectCount(null);
        stats.put("totalFeedback", totalFeedback);
        
        // 各评分统计
        Map<Integer, Long> ratingStats = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            long count = feedbackMapper.selectCount(new LambdaQueryWrapper<Feedback>()
                    .eq(Feedback::getRating, rating));
            ratingStats.put(rating, count);
        }
        stats.put("ratingStats", ratingStats);
        
        // 平均评分
        List<Feedback> feedbacks = feedbackMapper.selectList(new LambdaQueryWrapper<Feedback>()
                .isNotNull(Feedback::getRating));
        if (!feedbacks.isEmpty()) {
            double avgRating = feedbacks.stream()
                    .mapToInt(Feedback::getRating)
                    .average()
                    .orElse(0.0);
            stats.put("avgRating", Math.round(avgRating * 10) / 10.0);
        } else {
            stats.put("avgRating", 0.0);
        }
        
        // 待处理反馈数
        long pendingFeedback = feedbackMapper.selectCount(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getStatus, 0));
        stats.put("pendingFeedback", pendingFeedback);
        
        return stats;
    }
}
