package com.corleone.query.model;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Direction;
import com.corleone.query.model.o.TAC;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBy {

    private TAC field;
    private Direction direction;

    @Override
    public String toString() {
        return StrUtil.emptyToDefault(field.getFieldAlias(), field.toString()) + StrUtil.SPACE + direction;
    }
}
