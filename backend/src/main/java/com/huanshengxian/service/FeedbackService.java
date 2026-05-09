package com.huanshengxian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huanshengxian.entity.Feedback;

/**
 * 反馈服务接口
 */
public interface FeedbackService extends IService<Feedback> {
    
    /**
     * 添加反馈
     */
    Feedback addFeedback(Long userId, Long productId, Long orderId, String type, Integer rating, String content, String images);
    
    /**
     * 获取商品反馈
     */
    Page<Feedback> getProductFeedbacks(Long productId, Integer page, Integer size);
    
    /**
     * 获取用户反馈
     */
    Page<Feedback> getUserFeedbacks(Long userId, Integer page, Integer size);
}
