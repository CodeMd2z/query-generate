package com.corleone.query.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Order;
import com.corleone.model.EntityConfig;
import com.corleone.query.dto.QueryDo;
import com.corleone.query.dto.ViewObject;
import com.corleone.query.enums.QueryResultEnum;
import com.corleone.query.model.Join;
import com.corleone.query.model.OrderBy;
import com.corleone.query.model.Where;
import com.corleone.query.model.o.TA;
import com.corleone.query.model.o.TAC;
import com.corleone.query.utils.EntityConfigUtil;

import javax.management.AttributeNotFoundException;
import javax.naming.NameNotFoundException;
import java.util.*;

public class QDConvertor {

    public static QueryDo convert(ViewObject form, boolean counting) {
        QueryDo result = null;
        if (form != null) {
            result = new QueryDo();
            List<TA> allTA = new ArrayList<>();
            List<TAC> allTAC = new ArrayList<>();
            Set<String> activeTarget = new HashSet<>();

            handleSelect(form, allTAC, activeTarget, result, counting);
            handleFrom(form, allTA, result);
            handleJoin(form, allTA, allTAC, result);
            handleWhere(form, allTAC, activeTarget, result);
            handleOrder(form, allTAC, activeTarget, result);
            // 过滤未引用join
            filterJoin(result, activeTarget);
            // 填充别名
            fillAll(allTA, allTAC, result);
        }
        return result;
    }

    private static void handleSelect(ViewObject form, List<TAC> allTAC, Set<String> activeTarget, QueryDo result, boolean counting) {
        if (CollUtil.isNotEmpty(form.getSelect())) {
            int size = form.getSelect().size();
            List<TAC> select = new ArrayList<>(size);
            for (ViewObject.Select efn : form.getSelect()) {
                TAC tac = TAC.of(efn.getName());
                boolean active = false;
                if (!BooleanUtil.isTrue(efn.getDisable())) {
                    select.add(tac);
                    active = true;
                }
                if (BooleanUtil.isTrue(efn.getIsMeasure())) {
                    tac.setFunc(efn.getAggregation());
                    result.setNeedGroupBy(true);
                    if (counting) {
                        active = false;
                    }
                }
                if (active) {
                    activeTarget.add(tac.getTarget());
                }
                allTAC.add(tac);
            }
            result.setSelect(select);
        }
        if (result.getSelect().isEmpty()) {
            result.setError(QueryResultEnum.NO_SELECT);
        }
    }

    private static void handleFrom(ViewObject form, List<TA> allTA, QueryDo result) {
        if (StrUtil.isNotBlank(form.getFrom())) {
            TA from = new TA(form.getFrom());
            result.setFrom(from);
            allTA.add(from);
        } else {
            result.setError(QueryResultEnum.NO_FROM);
        }
    }

    private static void handleJoin(ViewObject form, List<TA> allTA, List<TAC> allTAC, QueryDo result) {
        if (CollUtil.isNotEmpty(form.getJoin())) {
            List<Join> joinList = new ArrayList<>(form.getJoin().size());
            for (ViewObject.Join j : form.getJoin()) {
                Join join = new Join();
                String target = j.getTarget();
                TA table = new TA(target);
                join.setTable(table);
                allTA.add(table);

                if (CollUtil.isNotEmpty(j.getOn())) {
                    List<Join.On> onList = new ArrayList<>(j.getOn().size());
                    for (ViewObject.On o : j.getOn()) {
                        TAC left = TAC.of(o.getLeft());
                        TAC right = TAC.of(o.getRight());
                        // 有且只有一个target等于自己
                        if (Objects.equals(target, left.getTarget()) ^ Objects.equals(target, right.getTarget())) {
                            allTAC.add(left);
                            if (Objects.nonNull(right.getField())) {
                                allTAC.add(right);
                            }
                            Join.On on = new Join.On(left, StrUtil.emptyToDefault(o.getOperator(), "="), right);
                            onList.add(on);
                        }
                    }
                    join.setOn(onList);
                } else {
                    result.setError(QueryResultEnum.JOIN_ON_MISSING);
                }
                if (CollUtil.isNotEmpty(join.getOn())) {
                    joinList.add(join);
                }
            }
            result.setJoin(joinList);
        }
    }

    private static void handleWhere(ViewObject form, List<TAC> allTAC, Set<String> activeTarget, QueryDo result) {
        if (CollUtil.isNotEmpty(form.getWhere())) {
            List<Where> whereList = new ArrayList<>(form.getWhere().size());
            for (ViewObject.Filter filter : form.getWhere()) {
                Where where = new Where();
                TAC left;
                if (StrUtil.isNotEmpty(filter.getOperator())) {
                    where.setOperator(ObjectUtil.defaultIfNull(Where.getOpBy(filter.getOperator()), Where.Op.Eq));
                    left = TAC.of(filter.getName());
                } else {
                    String field = where.setOperatorBy(filter.getName());
                    left = TAC.of(field, allTAC);
                }
                if (StrUtil.isNotEmpty(left.getTarget())) {
                    where.setLeft(left);
                    activeTarget.add(left.getTarget());
                    allTAC.add(left);
                    where.setValueJson(filter.getValueJson());
                    whereList.add(where);
                }
            }
            result.setWhere(whereList);
        }
    }

    private static void handleOrder(ViewObject form, List<TAC> allTAC, Set<String> activeTarget, QueryDo result) {
        Order[] orders = form.getOrders();
        if (ArrayUtil.isNotEmpty(orders)) {
            List<OrderBy> orderByList = new ArrayList<>(orders.length);
            for (Order order : orders) {
                String field = order.getField();
                if (Objects.nonNull(field)) {
                    TAC tac = TAC.of(field, allTAC);
                    if (StrUtil.isNotEmpty(tac.getTarget())) {
                        activeTarget.add(tac.getTarget());
                        allTAC.add(tac);
                        orderByList.add(new OrderBy(tac, order.getDirection()));
                    }
                }
            }
            result.setOrder(orderByList);
        }
        result.setPage(form);
    }

    private static void filterJoin(QueryDo result, Set<String> activeTarget) {
        Map<String, Integer> relateCount = new HashMap<>();
        Map<String, List<String>> relateMap = new HashMap<>();
        if (CollUtil.isNotEmpty(result.getJoin())) {
            for (Join join : result.getJoin()) {
                List<String> relates = new ArrayList<>();
                String target = join.getTable().getTarget();
                for (Join.On on : join.getOn()) {
                    String relateTarget = Objects.equals(target, on.getLeft().getTarget()) ? on.getRight().getTarget() : on.getLeft().getTarget();
                    relates.add(relateTarget);
                    relateCount.put(relateTarget, relateCount.getOrDefault(relateTarget, 0) + 1);
                }
                relateMap.put(target, relates);
            }
            Set<String> needRemove = new HashSet<>();
            for (String target : relateMap.keySet()) {
                Integer count = relateCount.getOrDefault(target, 0);
                backtrace(activeTarget, relateMap, relateCount, target, count, needRemove);
            }
            if (CollUtil.isNotEmpty(needRemove)) {
                result.getJoin().removeIf(o -> needRemove.contains(o.getTable().getTarget()));
            }
        }
    }

    private static void backtrace(Set<String> activeTarget, Map<String, List<String>> relateMap, Map<String, Integer> relateCount, String target, Integer count, Set<String> needRemove) {
        if (count == 0 && !activeTarget.contains(target) && !needRemove.contains(target)) {
            needRemove.add(target);
            List<String> relates = relateMap.get(target);
            if (CollUtil.isNotEmpty(relates)) {
                for (String relateTarget : relates) {
                    Integer c = relateCount.get(relateTarget);
                    relateCount.put(relateTarget, c - 1);
                    backtrace(activeTarget, relateMap, relateCount, relateTarget, c - 1, needRemove);
                }
            }
        }
    }

    private static final String TA = "t";

    public static void fillAll(List<TA> taList, List<TAC> tacList, QueryDo result) {
        int count = 1;
        Map<String, String> aliasMap = new HashMap<>();

        Map<String, EntityConfig> configMap = new HashMap<>();
        for (TA ta : taList) {
            String alias = TA + count++;
            ta.setAlias(alias);
            aliasMap.put(ta.getTarget(), alias);
            String entity = ta.getEntity();
            EntityConfig config = configMap.get(entity);
            if (Objects.isNull(config)) {
                config = EntityConfigUtil.get(entity);
                if (Objects.isNull(config)) {
                    throw new RuntimeException(new NameNotFoundException(entity));
                }
                configMap.put(entity, config);
            }
            ta.setTable(config.getTable());
        }

        for (TAC tac : tacList) {
            tac.setTableAlias(aliasMap.get(tac.getTarget()));
            String entity = tac.getEntity();
            EntityConfig config = configMap.get(entity);
            if (Objects.isNull(config)) {
                result.setError(QueryResultEnum.JOIN_MISSING, entity);
                return;
            }
            String field = tac.getField();
            String column = config.getColumns().get(field);
            if (StrUtil.isEmpty(column)) {
                throw new RuntimeException(new AttributeNotFoundException(field));
            }
            tac.setColumn(column);
            tac.setNum(config.getNumFields().contains(tac.getField()));
        }
    }
}
