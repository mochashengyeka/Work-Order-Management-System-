package com.workorder.enums;

import lombok.Getter;

@Getter
public enum ActionTypeEnum {
    CREATE(1, "创建工单"),
    ASSIGN(2, "派单"),
    GRAB(3, "抢单"),
    START(4, "开始处理"),
    FINISH(5, "完成维修"),
    CONFIRM(6, "确认验收"),
    CANCEL(7, "取消工单"),
    EVALUATE(8, "评价"),
    URGE(9, "催单");

    private final Integer code;
    private final String desc;

    ActionTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        if (code == null) return "未知";
        for (ActionTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e.getDesc();
            }
        }
        return "未知";
    }
}