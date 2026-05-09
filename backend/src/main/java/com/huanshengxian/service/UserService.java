package com.huanshengxian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huanshengxian.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);
    
    /**
     * 用户注册
     */
    User register(String username, String password, String nickname, String phone);
    
    /**
     * 用户登录
     */
    String login(String username, String password);
}
