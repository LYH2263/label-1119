package com.huanshengxian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售数据分析实体
 */
@Data
@TableName("sales_analytics")
public class SalesAnalytics {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long productId;
    
    private Long categoryId;
    
    private Long supplierId;
    
    private LocalDate date;
    
    private Integer salesCount;
    
    private BigDecimal salesAmount;
    
    private Integer orderCount;
    
    private BigDecimal avgRating;
    
    private Integer feedbackCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // 关联信息
    @TableField(exist = false)
    private String productName;
    
    @TableField(exist = false)
    private String categoryName;
    
    @TableField(exist = false)
    private String supplierName;
}
