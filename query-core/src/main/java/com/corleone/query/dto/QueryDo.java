package com.corleone.query.dto;

import cn.hutool.db.Page;
import com.corleone.query.core.QueryGenerator;
import com.corleone.query.model.Join;
import com.corleone.query.model.OrderBy;
import com.corleone.query.model.QueryResult;
import com.corleone.query.model.Where;
import com.corleone.query.model.o.TA;
import com.corleone.query.model.o.TAC;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class QueryDo extends QueryResult {

    private List<TAC> select;
    private Boolean needGroupBy = false;

    private TA from;

    private List<Join> join;

    private List<Where> where;

    private Page page;
    private List<OrderBy> order;

    public String toSql(boolean counting) {
        return toSql(counting, false);
    }

    public String toSql(boolean counting, boolean ignoreLimit) {
        return QueryGenerator.toSql(this, counting, ignoreLimit);
    }
}
