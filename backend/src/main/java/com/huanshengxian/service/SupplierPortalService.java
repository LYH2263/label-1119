package com.huanshengxian.service;

import com.huanshengxian.entity.Feedback;
import com.huanshengxian.entity.Supplier;

import java.util.List;
import java.util.Map;

/**
 * 供应商门户服务
 */
public interface SupplierPortalService {

    /**
     * 根据当前登录用户获取供应商信息
     */
    Supplier getSupplierByUserId(Long userId);

    /**
     * 更新供应商资料
     */
    Supplier updateSupplierProfile(Long userId, Map<String, Object> params);

    /**
     * 获取供应商工作台
     */
    Map<String, Object> getSupplierDashboard(Long userId);

    /**
     * 获取供应商反馈列表
     */
    List<Feedback> getSupplierFeedbacks(Long userId);

    /**
     * 供应商回复反馈
     */
    Feedback replyFeedback(Long userId, Long feedbackId, String reply);

    /**
     * 获取供应商数据反馈摘要
     */
    List<Map<String, Object>> getSupplierInsights();
}
