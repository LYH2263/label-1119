package com.huanshengxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huanshengxian.entity.Category;
import com.huanshengxian.entity.Product;
import com.huanshengxian.entity.Supplier;
import com.huanshengxian.mapper.CategoryMapper;
import com.huanshengxian.mapper.ProductMapper;
import com.huanshengxian.mapper.SupplierMapper;
import com.huanshengxian.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品服务实现
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final CategoryMapper categoryMapper;
    private final SupplierMapper supplierMapper;

    @Override
    public Page<Product> pageProducts(Integer page, Integer size, Long categoryId, String keyword,
                                       Integer isHot, Integer isNew, Integer isRecommend) {
        Page<Product> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword)
                   .or()
                   .like(Product::getDescription, keyword);
        }
        if (isHot != null) {
            wrapper.eq(Product::getIsHot, isHot);
        }
        if (isNew != null) {
            wrapper.eq(Product::getIsNew, isNew);
        }
        if (isRecommend != null) {
            wrapper.eq(Product::getIsRecommend, isRecommend);
        }
        
        wrapper.orderByDesc(Product::getSales);
        
        Page<Product> result = this.page(pageParam, wrapper);
        
        // 填充分类和供应商名称
        result.getRecords().forEach(this::fillProductInfo);
        
        return result;
    }

    @Override
    public List<Product> getHotProducts(Integer limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .eq(Product::getIsHot, 1)
               .orderByDesc(Product::getSales)
               .last("LIMIT " + limit);
        
        List<Product> products = this.list(wrapper);
        products.forEach(this::fillProductInfo);
        return products;
    }

    @Override
    public List<Product> getNewProducts(Integer limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .eq(Product::getIsNew, 1)
               .orderByDesc(Product::getCreatedAt)
               .last("LIMIT " + limit);
        
        List<Product> products = this.list(wrapper);
        products.forEach(this::fillProductInfo);
        return products;
    }

    @Override
    public Product getProductDetail(Long id) {
        Product product = this.getById(id);
        if (product != null) {
            fillProductInfo(product);
        }
        return product;
    }
    
    private void fillProductInfo(Product product) {
        if (product.getCategoryId() != null) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                product.setCategoryName(category.getName());
            }
        }
        if (product.getSupplierId() != null) {
            Supplier supplier = supplierMapper.selectById(product.getSupplierId());
            if (supplier != null) {
                product.setSupplierName(supplier.getName());
            }
        }
    }
}
