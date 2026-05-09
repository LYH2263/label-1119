package com.huanshengxian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huanshengxian.entity.Product;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {
    
    /**
     * 分页查询商品
     */
    Page<Product> pageProducts(Integer page, Integer size, Long categoryId, String keyword, Integer isHot, Integer isNew, Integer isRecommend);
    
    /**
     * 获取热销商品
     */
    List<Product> getHotProducts(Integer limit);
    
    /**
     * 获取新品推荐
     */
    List<Product> getNewProducts(Integer limit);
    
    /**
     * 获取商品详情
     */
    Product getProductDetail(Long id);
}
