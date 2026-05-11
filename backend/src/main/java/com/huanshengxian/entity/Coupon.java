package com.huanshengxian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("coupons")
public class Coupon {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Integer type;
    
    private BigDecimal amount;
    
    private BigDecimal discount;
    
    private BigDecimal minAmount;
    
    private Integer totalCount;
    
    private Integer receivedCount;
    
    private Integer usedCount;
    
    private LocalDateTime validStartTime;
    
    private LocalDateTime validEndTime;
    
    private Integer status;
    
    private String description;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private LocalDateTime deletedAt;
    
    public static final int TYPE_FULL_REDUCTION = 1;
    public static final int TYPE_DISCOUNT = 2;
    public static final int TYPE_NO_THRESHOLD = 3;
    
    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;
}
