package com.workorder.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SparePartVO {
    private Long id;
    private String partCode;
    private String partName;
    private String category;
    private String specification;
    private String unit;
    private Integer stockQuantity;
    private BigDecimal price;
    private String supplier;
}