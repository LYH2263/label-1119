package com.huanshengxian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huanshengxian.common.Result;
import com.huanshengxian.entity.Address;
import com.huanshengxian.mapper.AddressMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器
 */
@Tag(name = "地址管理", description = "收货地址相关接口")
@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressMapper addressMapper;

    @Operation(summary = "获取用户地址列表")
    @GetMapping
    public Result<List<Address>> getUserAddresses(@RequestAttribute Long userId) {
        List<Address> addresses = addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreatedAt));
        return Result.success(addresses);
    }

    @Operation(summary = "添加地址")
    @PostMapping
    public Result<Address> addAddress(@RequestAttribute Long userId,
                                      @RequestBody Address address) {
        address.setUserId(userId);
        
        // 如果是默认地址，取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.selectList(new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .eq(Address::getIsDefault, 1))
                    .forEach(a -> {
                        a.setIsDefault(0);
                        addressMapper.updateById(a);
                    });
        }
        
        addressMapper.insert(address);
        return Result.success("添加成功", address);
    }

    @Operation(summary = "更新地址")
    @PutMapping("/{id}")
    public Result<Address> updateAddress(@RequestAttribute Long userId,
                                         @PathVariable Long id,
                                         @RequestBody Address address) {
        Address existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("地址不存在");
        }
        
        // 如果是默认地址，取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.selectList(new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .eq(Address::getIsDefault, 1)
                    .ne(Address::getId, id))
                    .forEach(a -> {
                        a.setIsDefault(0);
                        addressMapper.updateById(a);
                    });
        }
        
        address.setId(id);
        address.setUserId(userId);
        addressMapper.updateById(address);
        return Result.success("更新成功", address);
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@RequestAttribute Long userId,
                                      @PathVariable Long id) {
        Address existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("地址不存在");
        }
        
        addressMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultAddress(@RequestAttribute Long userId,
                                          @PathVariable Long id) {
        Address existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("地址不存在");
        }
        
        // 取消其他默认地址
        addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1))
                .forEach(a -> {
                    a.setIsDefault(0);
                    addressMapper.updateById(a);
                });
        
        existing.setIsDefault(1);
        addressMapper.updateById(existing);
        return Result.success();
    }
}
