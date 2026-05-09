package com.huanshengxian.controller;

import com.huanshengxian.common.Result;
import com.huanshengxian.entity.Banner;
import com.huanshengxian.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图控制器
 */
@Tag(name = "轮播图管理", description = "轮播图相关接口")
@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @Operation(summary = "获取轮播图列表")
    @GetMapping
    public Result<List<Banner>> getBanners() {
        return Result.success(bannerService.getActiveBanners());
    }
}
