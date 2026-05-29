package com.workorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("part_usage")
public class PartUsage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long partId;
    private Integer quantity;
    private Long engineerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}