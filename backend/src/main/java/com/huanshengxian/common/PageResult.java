package com.huanshengxian.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    
    private Long total;
    private Integer pages;
    private Integer current;
    private Integer size;
    private List<T> records;

    public static <T> PageResult<T> of(Long total, Integer pages, Integer current, Integer size, List<T> records) {
        return new PageResult<>(total, pages, current, size, records);
    }
}
