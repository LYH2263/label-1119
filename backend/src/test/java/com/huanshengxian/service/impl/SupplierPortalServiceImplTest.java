package com.huanshengxian.service.impl;

import com.huanshengxian.entity.Feedback;
import com.huanshengxian.entity.Product;
import com.huanshengxian.entity.SalesAnalytics;
import com.huanshengxian.entity.Supplier;
import com.huanshengxian.mapper.FeedbackMapper;
import com.huanshengxian.mapper.ProductMapper;
import com.huanshengxian.mapper.SalesAnalyticsMapper;
import com.huanshengxian.mapper.SupplierMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierPortalServiceImplTest {

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private FeedbackMapper feedbackMapper;

    @Mock
    private SalesAnalyticsMapper salesAnalyticsMapper;

    @InjectMocks
    private SupplierPortalServiceImpl supplierPortalService;

    @Test
    void shouldGenerateSupplierInsightsWithRecommendations() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("阳光农场");
        supplier.setContactPerson("王大明");
        supplier.setAddress("山东寿光");
        supplier.setStatus(1);

        Product product = new Product();
        product.setId(10L);
        product.setSupplierId(1L);
        product.setName("新鲜菠菜");
        product.setSales(1288);
        product.setStatus(1);

        Feedback feedback = new Feedback();
        feedback.setId(100L);
        feedback.setProductId(10L);
        feedback.setRating(2);
        feedback.setContent("包装和口感都需要改进");
        feedback.setStatus(0);

        SalesAnalytics analytics = new SalesAnalytics();
        analytics.setSupplierId(1L);
        analytics.setDate(LocalDate.of(2026, 3, 1));
        analytics.setSalesAmount(new BigDecimal("2560.00"));
        analytics.setSalesCount(180);
        analytics.setOrderCount(96);

        when(supplierMapper.selectList(any())).thenReturn(List.of(supplier));
        when(productMapper.selectList(any())).thenReturn(List.of(product));
        when(feedbackMapper.selectList(any())).thenReturn(List.of(feedback));
        when(salesAnalyticsMapper.selectList(any())).thenReturn(List.of(analytics));

        List<java.util.Map<String, Object>> result = supplierPortalService.getSupplierInsights();

        assertEquals(1, result.size());
        assertEquals("阳光农场", result.get(0).get("supplierName"));
        assertTrue(((List<?>) result.get(0).get("recommendations")).size() >= 2);
        assertTrue(((List<?>) result.get(0).get("hotProducts")).contains("新鲜菠菜"));
    }

    @Test
    void shouldRejectReplyWhenFeedbackDoesNotBelongToSupplier() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setUserId(99L);
        supplier.setStatus(1);

        Feedback feedback = new Feedback();
        feedback.setId(200L);
        feedback.setProductId(300L);

        Product product = new Product();
        product.setId(300L);
        product.setSupplierId(2L);

        when(supplierMapper.selectOne(any())).thenReturn(supplier);
        when(feedbackMapper.selectById(200L)).thenReturn(feedback);
        when(productMapper.selectById(300L)).thenReturn(product);

        RuntimeException error = assertThrows(RuntimeException.class,
                () -> supplierPortalService.replyFeedback(99L, 200L, "请继续优化"));

        assertEquals("无权回复该反馈", error.getMessage());
        verify(feedbackMapper, never()).updateById(any());
    }
}
