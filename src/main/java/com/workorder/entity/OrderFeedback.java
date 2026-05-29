package com.workorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("order_feedback")
public class OrderFeedback {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Integer rating;
    private String content;

    @TableField(typeHandler = com.workorder.handler.JsonTypeHandler.class)
    private String tags;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}