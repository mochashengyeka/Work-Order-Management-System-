package com.workorder.exception;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Result {
    private Integer code;
    private String message;
    private Object data;

    public static Result success() {
        return new Result().setCode(200).setMessage("操作成功");
    }

    public static Result success(Object data) {
        return new Result().setCode(200).setMessage("操作成功").setData(data);
    }

    public static Result success(String message, Object data) {
        return new Result().setCode(200).setMessage(message).setData(data);
    }

    public static Result error(Integer code, String message) {
        return new Result().setCode(code).setMessage(message);
    }

    public static Result error(String message) {
        return new Result().setCode(500).setMessage(message);
    }
}