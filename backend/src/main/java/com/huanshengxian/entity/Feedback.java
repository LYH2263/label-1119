package com.huanshengxian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消费者反馈实体
 */
@Data
@TableName("feedbacks")
public class Feedback {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long productId;
    
    private Long orderId;
    
    private String type;
    
    private Integer rating;
    
    private String content;
    
    private String images;
    
    private String reply;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // 关联信息
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private String productName;
}
