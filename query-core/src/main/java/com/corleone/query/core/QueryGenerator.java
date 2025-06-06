package com.corleone.query.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Page;
import com.corleone.query.dto.QueryDo;
import com.corleone.query.model.Join;
import com.corleone.query.model.OrderBy;
import com.corleone.query.model.Where;
import com.corleone.query.model.o.TAC;

import java.util.*;

public class QueryGenerator {
    private static final String PK = "id";

    private static final String SELECT = "select";
    private static final String COUNT_ALL = "count(*)";
    private static final String FROM = "from";
    private static final String LEFT_JOIN = "left join";
    private static final String ON = "on";
    private static final String WHERE = "where";
    private static final String GROUP_BY = "group by";
    private static final String ORDER_BY = "order by";
    private static final String LIMIT = "limit";
    private static final String OFFSET = "offset";

    public static String toSql(QueryDo queryDo, boolean counting, boolean ignoreLimit) {
        List<String> sb = new ArrayList<>();
        List<TAC> select = CollUtil.emptyIfNull(queryDo.getSelect());
        sb.add(SELECT);
        if (counting) {
            sb.add(COUNT_ALL);
        } else {
            sb.add(TAC.toString(select));
        }
        sb.add(FROM);
        sb.add(queryDo.getFrom().toString());
        List<Join> join = queryDo.getJoin();
        if (Objects.nonNull(join)) {
            for (Join j : join) {
                if (Objects.nonNull(j.getOn())) {
                    sb.add(LEFT_JOIN);
                    sb.add(j.getTable().toString());
                    sb.add(ON);
                    sb.add(j.onStr());
                }
            }
        }
        // where
        List<Where> where = queryDo.getWhere();
        if (Objects.nonNull(where)) {
            String string = Where.toString(where);
            if (StrUtil.isNotBlank(string)) {
                sb.add(WHERE);
                sb.add(string);
            }
        }
        if (!counting) {
            // group by
            if (queryDo.getNeedGroupBy()) {
                List<TAC> groupBy = new ArrayList<>(select.size());
                Set<String> skipTarget = new HashSet<>();
                for (TAC s : select) {
                    if (skipTarget.contains(s.getTarget())) {
                        continue;
                    }
                    if (!s.hasFunc()) {
                        if (Objects.equals(s.getColumn(), PK)) {
                            skipTarget.add(s.getTarget());
                        }
                        groupBy.add(s);
                    }
                }
                if (!groupBy.isEmpty()) {
                    sb.add(GROUP_BY);
                    sb.add(CollUtil.join(groupBy, StrUtil.COMMA, TAC::name));
                }
            }
            // page
            Page page = queryDo.getPage();
            if (Objects.isNull(page)) {
                page = new Page();
            }
            List<OrderBy> order = queryDo.getOrder();
            if (CollUtil.isNotEmpty(order)) {
                sb.add(ORDER_BY);
                sb.add(CollUtil.join(order, StrUtil.COMMA));
            }
            if (!ignoreLimit) {
                int offset = page.getStartIndex();
                int limit = page.getPageSize();
                sb.add(LIMIT);
                sb.add(Integer.toString(limit));
                sb.add(OFFSET);
                sb.add(Integer.toString(offset));
            }
        }
        return CollUtil.join(sb, StrUtil.SPACE);
    }
}
