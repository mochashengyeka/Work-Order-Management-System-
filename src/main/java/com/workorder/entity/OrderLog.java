package com.workorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("order_log")
public class OrderLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long operatorId;
    private Integer operatorRole;
    private Integer actionType;
    private Integer fromStatus;
    private Integer toStatus;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}