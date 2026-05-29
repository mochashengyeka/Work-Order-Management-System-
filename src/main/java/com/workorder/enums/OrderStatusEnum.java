package com.workorder.enums;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {
    PENDING_ASSIGN(1, "待派单"),
    PENDING_ACCEPT(2, "待接单"),
    PROCESSING(3, "处理中"),
    PENDING_CONFIRM(4, "待验收"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消");

    private final Integer code;
    private final String desc;

    OrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        if (code == null) return "未知";
        for (OrderStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e.getDesc();
            }
        }
        return "未知";
    }
}