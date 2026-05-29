// UserVO.java
package com.workorder.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String avatar;
    private Integer role;
    private Integer status;
}