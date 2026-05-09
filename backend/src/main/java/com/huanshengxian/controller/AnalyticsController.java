package com.huanshengxian.controller;

import com.huanshengxian.common.Result;
import com.huanshengxian.entity.SalesAnalytics;
import com.huanshengxian.service.AnalyticsService;
import com.huanshengxian.service.SupplierPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据分析控制器
 */
@Tag(name = "数据分析", description = "销售数据分析相关接口")
@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SupplierPortalService supplierPortalService;

    @Operation(summary = "获取销售概览")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getSalesOverview() {
        return Result.success(analyticsService.getSalesOverview());
    }

    @Operation(summary = "获取热销商品排行")
    @GetMapping("/hot-products")
    public Result<List<Map<String, Object>>> getHotProductsRanking(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(analyticsService.getHotProductsRanking(limit));
    }

    @Operation(summary = "获取分类销售统计")
    @GetMapping("/category-sales")
    public Result<List<Map<String, Object>>> getCategorySales() {
        return Result.success(analyticsService.getCategorySales());
    }

    @Operation(summary = "获取销售趋势")
    @GetMapping("/sales-trend")
    public Result<List<SalesAnalytics>> getSalesTrend(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(analyticsService.getSalesTrend(startDate, endDate));
    }

    @Operation(summary = "获取用户反馈统计")
    @GetMapping("/feedback-stats")
    public Result<Map<String, Object>> getFeedbackStats() {
        return Result.success(analyticsService.getFeedbackStats());
    }

    @Operation(summary = "获取供应商数据反馈摘要")
    @GetMapping("/supplier-insights")
    public Result<List<Map<String, Object>>> getSupplierInsights() {
        return Result.success(supplierPortalService.getSupplierInsights());
    }
}
