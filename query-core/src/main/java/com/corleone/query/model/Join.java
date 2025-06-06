package com.corleone.query.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.corleone.query.model.o.TA;
import com.corleone.query.model.o.TAC;
import com.corleone.query.model.o.TACV;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
public class Join {

    private TA table;
    private List<On> on;

    @Data
    public static class On {
        private TAC left;
        private String operator;
        private TAC right;

        public On(TAC left, String operator, TAC right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public String toString() {
            String str = left + StrUtil.SPACE;
            String opName = operator;
            Where.Op op = Where.getOpBy(operator);
            if (Objects.nonNull(op)) {
                opName = op.code;
            }
            str += opName + StrUtil.SPACE;
            if (Objects.isNull(op) || !op.noValue) {
                if (right instanceof TACV r) {
                    str += Where.formatValue(left, r.toString());
                } else {
                    str += right;
                }
            }
            return str;
        }
    }

    public String onStr() {
        return CollUtil.join(on, " and ", On::toString);
    }

}
