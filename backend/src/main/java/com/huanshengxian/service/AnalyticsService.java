package com.huanshengxian.service;

import com.huanshengxian.entity.SalesAnalytics;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据分析服务接口
 */
public interface AnalyticsService {
    
    /**
     * 获取销售概览
     */
    Map<String, Object> getSalesOverview();
    
    /**
     * 获取热销商品排行
     */
    List<Map<String, Object>> getHotProductsRanking(Integer limit);
    
    /**
     * 获取分类销售统计
     */
    List<Map<String, Object>> getCategorySales();
    
    /**
     * 获取销售趋势
     */
    List<SalesAnalytics> getSalesTrend(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户反馈统计
     */
    Map<String, Object> getFeedbackStats();
}
