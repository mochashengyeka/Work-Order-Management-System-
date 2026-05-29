package com.workorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("spare_part")
public class SparePart {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String partCode;
    private String partName;
    private String category;
    private String specification;
    private String unit;
    private Integer stockQuantity;
    private Integer minStock;
    private BigDecimal price;
    private String supplier;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}