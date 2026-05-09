package com.huanshengxian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huanshengxian.entity.Banner;

import java.util.List;

/**
 * 轮播图服务接口
 */
public interface BannerService extends IService<Banner> {
    
    /**
     * 获取启用的轮播图
     */
    List<Banner> getActiveBanners();
}
