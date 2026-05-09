package com.huanshengxian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huanshengxian.entity.Category;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService extends IService<Category> {
    
    /**
     * 获取分类树
     */
    List<Category> getCategoryTree();
    
    /**
     * 获取一级分类
     */
    List<Category> getTopCategories();
    
    /**
     * 获取子分类
     */
    List<Category> getChildCategories(Long parentId);
}
