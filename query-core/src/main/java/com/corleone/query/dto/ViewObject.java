package com.corleone.query.dto;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.db.Page;
import cn.hutool.db.sql.Order;
import com.corleone.query.model.Where;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ViewObject extends Page {

    private List<Select> select;
    private String from;
    private List<Join> join;
    private List<Filter> where;

    @Data
    public static class Select {
        String name;
        Boolean disable;
        Boolean isMeasure;
        String aggregation;
    }

    @Data
    public static class Join {
        /**
         * Entity or Entity__1 or Entity__2
         */
        String target;
        List<On> on;
    }

    @Data
    public static class On {
        String left;
        String operator;
        String right;
    }

    @Data
    @NoArgsConstructor
    public static class Filter {
        String name;
        String operator;
        String value;
        String valueJson;

        public String getV() {
            return ObjectUtil.defaultIfNull(value, valueJson);
        }

        public Filter(String name, String valueJson) {
            this.name = name;
            this.valueJson = valueJson;
        }
    }

    public void setOrders(Order[] orders) {
        super.setOrder(orders);
    }

    public static ViewObject of() {
        return new ViewObject();
    }

    public ViewObject where(String key, Where.Op op, Object value) {
        if (CollUtil.isEmpty(where)) {
            where = new ArrayList<>();
        }
        where.add(new Filter(key, value.toString()));
        return this;
    }
}
