package com.huanshengxian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huanshengxian.common.Result;
import com.huanshengxian.entity.Feedback;
import com.huanshengxian.entity.Supplier;
import com.huanshengxian.mapper.SupplierMapper;
import com.huanshengxian.service.SupplierPortalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 供应商控制器
 */
@Tag(name = "供应商管理", description = "供应商相关接口")
@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierMapper supplierMapper;
    private final SupplierPortalService supplierPortalService;

    @Operation(summary = "获取供应商列表")
    @GetMapping
    public Result<List<Supplier>> getSuppliers() {
        List<Supplier> suppliers = supplierMapper.selectList(new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getStatus, 1));
        return Result.success(suppliers);
    }

    @Operation(summary = "获取供应商详情")
    @GetMapping("/{id}")
    public Result<Supplier> getSupplierDetail(@PathVariable Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            return Result.error("供应商不存在");
        }
        return Result.success(supplier);
    }

    @Operation(summary = "获取当前供应商工作台")
    @GetMapping("/me/dashboard")
    public Result<Map<String, Object>> getSupplierDashboard(@RequestAttribute Long userId) {
        return Result.success(supplierPortalService.getSupplierDashboard(userId));
    }

    @Operation(summary = "获取当前供应商反馈列表")
    @GetMapping("/me/feedbacks")
    public Result<List<Feedback>> getSupplierFeedbacks(@RequestAttribute Long userId) {
        return Result.success(supplierPortalService.getSupplierFeedbacks(userId));
    }

    @Operation(summary = "更新当前供应商资料")
    @PutMapping("/me")
    public Result<Supplier> updateSupplierProfile(@RequestAttribute Long userId,
                                                  @RequestBody Map<String, Object> params) {
        return Result.success("更新成功", supplierPortalService.updateSupplierProfile(userId, params));
    }

    @Operation(summary = "供应商回复消费者反馈")
    @PutMapping("/me/feedbacks/{feedbackId}/reply")
    public Result<Feedback> replyFeedback(@RequestAttribute Long userId,
                                          @PathVariable Long feedbackId,
                                          @RequestBody Map<String, String> params) {
        String reply = params.get("reply");
        return Result.success("回复成功", supplierPortalService.replyFeedback(userId, feedbackId, reply));
    }
}
