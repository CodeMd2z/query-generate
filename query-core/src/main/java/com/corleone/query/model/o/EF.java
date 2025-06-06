package com.corleone.query.model.o;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity and field
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public abstract class EF extends E {

    public static final String AS = " as ";

    String field;
    String fieldAlias;
    boolean isNum;

    public EF(String target, String field) {
        super(target);
        int asIndex = field.indexOf(AS);
        if (asIndex > 0) {
            fieldAlias = field.substring(asIndex + AS.length());
            field = field.substring(0, asIndex);
        }
        this.field = field;
    }

    public String fieldKey() {
        return StrUtil.emptyToDefault(fieldAlias, field);
    }

    public String as() {
        return fieldAlias == null ? StrUtil.EMPTY : AS + fieldAlias;
//        return AS + fieldKey();
    }
}
