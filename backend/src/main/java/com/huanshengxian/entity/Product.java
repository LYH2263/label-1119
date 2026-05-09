package com.huanshengxian.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("products")
public class Product {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long categoryId;
    
    private Long supplierId;
    
    private String name;
    
    private String description;
    
    private BigDecimal price;
    
    private BigDecimal originalPrice;
    
    private String unit;
    
    private Integer stock;
    
    private Integer sales;
    
    private String images;
    
    private String origin;
    
    private String shelfLife;
    
    private String storageMethod;
    
    private Integer isHot;
    
    private Integer isNew;
    
    private Integer isRecommend;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private String categoryName;
    
    @TableField(exist = false)
    private String supplierName;
}
