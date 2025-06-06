package com.corleone.query.enums;

import lombok.Getter;

public enum QueryResultEnum {
    SUCCESS(200, "成功"),
    FAILURE(400, "失败"),
    FORBIDDEN(403, "您没有权限"),

    NO_CHANGE(1000, "没有修改"),

    NO_SELECT(2001, "Select is empty"),
    NO_FROM(2002, "From is empty"),
    JOIN_MISSING(2003, "Miss Join Entity"),
    JOIN_ON_MISSING(2004, "Miss Join On Condition"),
    ;

    @Getter
    private final Integer code;
    private final String msg;

    QueryResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getMessage() {
        return msg;
    }
}