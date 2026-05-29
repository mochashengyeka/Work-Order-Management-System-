package com.workorder.dto;

import lombok.Data;

@Data
public class PageDTO {
    private Integer page = 1;   // 当前页码
    private Integer size = 10;  // 每页条数
    private String sortField;   // 排序字段
    private String sortOrder;   // 排序方式（asc/desc）
}