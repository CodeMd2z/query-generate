package com.corleone.query.model.o;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

/**
 * Column with table and tableAlias
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class TAC extends EF {
    private static final String FUNC = "%s(%s)";

    private String tableAlias;
    private String column;
    private String func;

    TAC(String target, String field) {
        super(target, field);
    }

    public static TAC of(String name) {
        return of(name, null);
    }

    public static TAC of(String name, List<TAC> others) {
        if (Objects.nonNull(name)) {
            int index = name.indexOf(StrUtil.DOT);
            if (index < 0) {
                if (Objects.nonNull(others)) {
                    for (TAC tac : others) {
                        if (Objects.equals(name, tac.fieldKey())) {
                            return BeanUtil.copyProperties(tac, TAC.class);
                        }
                    }
                }
                return new TACV(name);
            } else {
                return new TAC(name.substring(0, index),
                        name.substring(index + 1));
            }
        }
        return new TAC();
    }

    @Override
    public String toString() {
        return name() + as();
    }

    public String name() {
        String name = tableAlias + StrUtil.DOT + column;
        if (hasFunc()) {
            name = String.format(FUNC, func, name);
        }
        return name;
    }

    public boolean hasFunc() {
        return StrUtil.isNotEmpty(func);
    }

    public static String toString(List<TAC> tacList) {
        return CollUtil.join(tacList, StrUtil.COMMA);
    }

    public String getResultKey() {
        return StrUtil.emptyToDefault(fieldAlias, column);
    }
}
