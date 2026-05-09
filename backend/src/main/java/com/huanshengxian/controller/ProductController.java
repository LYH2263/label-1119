package com.huanshengxian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huanshengxian.common.PageResult;
import com.huanshengxian.common.Result;
import com.huanshengxian.entity.Product;
import com.huanshengxian.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 */
@Tag(name = "商品管理", description = "商品相关接口")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "分页查询商品")
    @GetMapping
    public Result<PageResult<Product>> listProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer isHot,
            @RequestParam(required = false) Integer isNew,
            @RequestParam(required = false) Integer isRecommend) {
        
        Page<Product> result = productService.pageProducts(page, size, categoryId, keyword, isHot, isNew, isRecommend);
        PageResult<Product> pageResult = PageResult.of(
                result.getTotal(),
                (int) result.getPages(),
                (int) result.getCurrent(),
                (int) result.getSize(),
                result.getRecords()
        );
        
        return Result.success(pageResult);
    }

    @Operation(summary = "获取热销商品")
    @GetMapping("/hot")
    public Result<List<Product>> getHotProducts(@RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(productService.getHotProducts(limit));
    }

    @Operation(summary = "获取新品推荐")
    @GetMapping("/new")
    public Result<List<Product>> getNewProducts(@RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(productService.getNewProducts(limit));
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }
}
