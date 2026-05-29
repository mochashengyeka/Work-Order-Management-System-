package com.workorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("work_order")
public class WorkOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private String title;
    private String description;
    private Integer categoryId;

    /** 优先级：1-紧急 2-高 3-中 4-低 */
    private Integer priority;

    private Long reporterId;
    private Long reporterDeptId;
    private Long engineerId;

    /** 状态：1-待派单 2-待接单 3-处理中 4-待验收 5-已完成 6-已取消 */
    private Integer status;

    private LocalDateTime slaDeadline;

    @TableField(typeHandler = com.workorder.handler.JsonTypeHandler.class)
    private String images;  // JSON数组

    private String location;
    private String deviceInfo;

    // 工程师填写
    private String diagnosis;
    private String repairAction;

    @TableField(typeHandler = com.workorder.handler.JsonTypeHandler.class)
    private String partsUsed;

    @TableField(typeHandler = com.workorder.handler.JsonTypeHandler.class)
    private String repairImages;

    private BigDecimal actualCost;

    // 时间字段
    private LocalDateTime createTime;
    private LocalDateTime assignTime;
    private LocalDateTime acceptTime;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private LocalDateTime confirmTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String reporterName;
}