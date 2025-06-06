package com.corleone.query.model.o;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Table with alias
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TA extends E {
    private String table;
    private String alias;

    public TA(String target) {
        super(target);
    }

    @Override
    public String toString() {
        return table + StrUtil.SPACE + alias;
    }
}
