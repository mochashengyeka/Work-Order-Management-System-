package com.workorder.enums;

import lombok.Getter;

@Getter
public enum PriorityEnum {
    URGENT(1, "紧急", 4),
    HIGH(2, "高", 8),
    MEDIUM(3, "中", 24),
    LOW(4, "低", 48);

    private final Integer code;
    private final String desc;
    private final Integer slaHours;

    PriorityEnum(Integer code, String desc, Integer slaHours) {
        this.code = code;
        this.desc = desc;
        this.slaHours = slaHours;
    }
}