package com.huanshengxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huanshengxian.entity.Feedback;
import com.huanshengxian.entity.Product;
import com.huanshengxian.entity.User;
import com.huanshengxian.mapper.FeedbackMapper;
import com.huanshengxian.mapper.ProductMapper;
import com.huanshengxian.mapper.UserMapper;
import com.huanshengxian.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 反馈服务实现
 */
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @Override
    public Feedback addFeedback(Long userId, Long productId, Long orderId, String type, Integer rating, String content, String images) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setProductId(productId);
        feedback.setOrderId(orderId);
        feedback.setType(type);
        feedback.setRating(rating);
        feedback.setContent(content);
        feedback.setImages(images);
        feedback.setStatus(0);
        
        this.save(feedback);
        return feedback;
    }

    @Override
    public Page<Feedback> getProductFeedbacks(Long productId, Integer page, Integer size) {
        Page<Feedback> pageParam = new Page<>(page, size);
        
        Page<Feedback> result = this.page(pageParam, new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getProductId, productId)
                .orderByDesc(Feedback::getCreatedAt));
        
        result.getRecords().forEach(this::fillFeedbackInfo);
        
        return result;
    }

    @Override
    public Page<Feedback> getUserFeedbacks(Long userId, Integer page, Integer size) {
        Page<Feedback> pageParam = new Page<>(page, size);
        
        Page<Feedback> result = this.page(pageParam, new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getUserId, userId)
                .orderByDesc(Feedback::getCreatedAt));
        
        result.getRecords().forEach(this::fillFeedbackInfo);
        
        return result;
    }
    
    private void fillFeedbackInfo(Feedback feedback) {
        if (feedback.getUserId() != null) {
            User user = userMapper.selectById(feedback.getUserId());
            if (user != null) {
                feedback.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
        if (feedback.getProductId() != null) {
            Product product = productMapper.selectById(feedback.getProductId());
            if (product != null) {
                feedback.setProductName(product.getName());
            }
        }
    }
}
