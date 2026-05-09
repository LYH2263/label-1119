package com.huanshengxian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huanshengxian.common.PageResult;
import com.huanshengxian.common.Result;
import com.huanshengxian.entity.Feedback;
import com.huanshengxian.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 反馈控制器
 */
@Tag(name = "反馈管理", description = "消费者反馈相关接口")
@RestController
@RequestMapping("/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "添加反馈")
    @PostMapping
    public Result<Feedback> addFeedback(@RequestAttribute Long userId,
                                        @RequestBody Map<String, Object> params) {
        Long productId = params.get("productId") != null ? Long.valueOf(params.get("productId").toString()) : null;
        Long orderId = params.get("orderId") != null ? Long.valueOf(params.get("orderId").toString()) : null;
        String type = params.get("type") != null ? params.get("type").toString() : "PRODUCT";
        Integer rating = params.get("rating") != null ? Integer.valueOf(params.get("rating").toString()) : null;
        String content = params.get("content") != null ? params.get("content").toString() : "";
        String images = params.get("images") != null ? params.get("images").toString() : null;
        
        Feedback feedback = feedbackService.addFeedback(userId, productId, orderId, type, rating, content, images);
        return Result.success("反馈提交成功", feedback);
    }

    @Operation(summary = "获取商品反馈")
    @GetMapping("/product/{productId}")
    public Result<PageResult<Feedback>> getProductFeedbacks(@PathVariable Long productId,
                                                            @RequestParam(defaultValue = "1") Integer page,
                                                            @RequestParam(defaultValue = "10") Integer size) {
        Page<Feedback> result = feedbackService.getProductFeedbacks(productId, page, size);
        PageResult<Feedback> pageResult = PageResult.of(
                result.getTotal(),
                (int) result.getPages(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords()
        );
        return Result.success(pageResult);
    }

    @Operation(summary = "获取用户反馈")
    @GetMapping("/user")
    public Result<PageResult<Feedback>> getUserFeedbacks(@RequestAttribute Long userId,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        Page<Feedback> result = feedbackService.getUserFeedbacks(userId, page, size);
        PageResult<Feedback> pageResult = PageResult.of(
                result.getTotal(),
                (int) result.getPages(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords()
        );
        return Result.success(pageResult);
    }
}
