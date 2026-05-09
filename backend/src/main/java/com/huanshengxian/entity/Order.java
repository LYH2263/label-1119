package com.huanshengxian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体
 */
@Data
@TableName("orders")
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orderNo;
    
    private Long userId;
    
    private BigDecimal totalAmount;
    
    private BigDecimal payAmount;
    
    private BigDecimal freightAmount;
    
    private String status;
    
    private String receiverName;
    
    private String receiverPhone;
    
    private String receiverAddress;
    
    private String remark;
    
    private LocalDateTime payTime;
    
    private LocalDateTime shipTime;
    
    private LocalDateTime receiveTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // 订单项
    @TableField(exist = false)
    private List<OrderItem> items;
}
