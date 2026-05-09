package com.huanshengxian.controller;

import com.huanshengxian.common.Result;
import com.huanshengxian.entity.Category;
import com.huanshengxian.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@Tag(name = "分类管理", description = "商品分类相关接口")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<Category>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @Operation(summary = "获取一级分类")
    @GetMapping("/top")
    public Result<List<Category>> getTopCategories() {
        return Result.success(categoryService.getTopCategories());
    }

    @Operation(summary = "获取子分类")
    @GetMapping("/{parentId}/children")
    public Result<List<Category>> getChildCategories(@PathVariable Long parentId) {
        return Result.success(categoryService.getChildCategories(parentId));
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<Category> getCategoryDetail(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        return Result.success(category);
    }
}
