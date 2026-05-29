package com.workorder.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    REPORTER(1, "报修人"),
    ENGINEER(2, "维修工程师"),
    ADMIN(3, "管理员");

    private final Integer code;
    private final String desc;

    RoleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}