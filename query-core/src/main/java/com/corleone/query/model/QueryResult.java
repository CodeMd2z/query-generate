package com.corleone.query.model;

import cn.hutool.core.util.StrUtil;
import com.corleone.query.enums.QueryResultEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public abstract class QueryResult {

    private List<String> error;

    public void setError(QueryResultEnum e) {
        setError(e, null);
    }

    public void setError(QueryResultEnum e, String msg) {
        if (error == null) {
            error = new ArrayList<>();
        }
        error.add(e.getMessage() + (Objects.nonNull(msg) ? StrUtil.COLON + msg : StrUtil.EMPTY));
    }

    public boolean hasError() {
        return null != error && !error.isEmpty();
    }
}
