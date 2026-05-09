package com.huanshengxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huanshengxian.entity.Category;
import com.huanshengxian.mapper.CategoryMapper;
import com.huanshengxian.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> getCategoryTree() {
        List<Category> allCategories = this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));
        
        // 构建树结构
        List<Category> topCategories = allCategories.stream()
                .filter(c -> c.getParentId() == 0)
                .collect(Collectors.toList());
        
        topCategories.forEach(top -> {
            List<Category> children = allCategories.stream()
                    .filter(c -> c.getParentId().equals(top.getId()))
                    .collect(Collectors.toList());
            top.setChildren(children);
        });
        
        return topCategories;
    }

    @Override
    public List<Category> getTopCategories() {
        return this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .eq(Category::getParentId, 0)
                .orderByAsc(Category::getSortOrder));
    }

    @Override
    public List<Category> getChildCategories(Long parentId) {
        return this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .eq(Category::getParentId, parentId)
                .orderByAsc(Category::getSortOrder));
    }
}
