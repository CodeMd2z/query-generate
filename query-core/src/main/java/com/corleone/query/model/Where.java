package com.corleone.query.model;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONValidator;
import com.corleone.query.dto.RelativeDate;
import com.corleone.query.dto.RelativeDateRange;
import com.corleone.query.model.o.TAC;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.hutool.core.text.CharSequenceUtil.EMPTY;
import static cn.hutool.core.text.CharSequenceUtil.SPACE;

@Data
@NoArgsConstructor
public class Where {

    private static final String SINGLE_QUOTE = "'";
    private static final String D_BS = "\\\\";
    private static final String PERCENT = "%";
    private static final String AND = " and ";
    private static final String JSON_OBJECT = "Object";
    private static final Pattern compile = Pattern.compile("['\\\\]");

    private TAC left;
    private Op operator;
    private String valueJson;

    @Override
    public String toString() {
        if (operator.notNumber && left.isNum()) {
            throw new IllegalArgumentException(left.getField() + SPACE + operator);
        }
        if (Objects.isNull(valueJson)) {
            valueJson = EMPTY;
        }
        rewriteValue();
        String value;
        if (operator.noValue) {
            value = EMPTY;
        } else
            switch (operator) {
                case Like:
                case ILike:
                    int index = valueJson.indexOf(PERCENT);
                    if (index >= 0) {
                        value = valueJson;
                    } else {
                        value = StrUtil.wrap(valueJson, PERCENT);
                    }
                    value = escapeAndWrapQuote(value);
                    break;
                case In:
                case Between:
                    value = StrUtil.wrapIfMissing(valueJson, StrPool.BRACKET_START, StrPool.BRACKET_END);
                    if (!JSONValidator.from(value).validate()) {
                        return null;
                    } else {
                        List<?> list;
                        if (left.isNum()) {
                            list = JSONArray.parseArray(value, Number.class);
                        } else {
                            list = CollUtil.map(JSONArray.parseArray(value, String.class),
                                    Where::escapeAndWrapQuote, true);
                        }
                        if (list == null || list.isEmpty()) {
                            return null;
                        }
                        if (operator == Op.In) {
                            value = list.stream().map(Object::toString)
                                    .collect(Collectors.joining(StrUtil.COMMA, "(", ")"));
                        } else if (operator == Op.Between) {
                            if (list.size() != 2) {
                                return null;
                            }
                            value = CollUtil.getFirst(list) + AND + CollUtil.getLast(list);
                        } else {
                            return null;
                        }
                    }
                    break;
                default:
                    value = formatValue(left, valueJson);
                    break;
            }
        if (value == null) {
            return null;
        }
        return left.name() + SPACE + operator.code + SPACE + value;
    }

    public static String formatValue(TAC left, String value) {
        if (left.isNum()) {
            return NumberUtil.parseNumber(value).toString();
        } else {
            return escapeAndWrapQuote(value);
        }
    }

    public static String escapeAndWrapQuote(String value) {
        if (Objects.nonNull(value)) {
            if (compile.matcher(value).find()) {
                value = value.replaceAll(D_BS, D_BS + D_BS);
                value = value.replaceAll(SINGLE_QUOTE, D_BS + "'");
                return 'E' + StrUtil.wrap(value, SINGLE_QUOTE);
            }
        }
        return StrUtil.wrap(value, SINGLE_QUOTE);
    }

    private void rewriteValue() {
        JSONValidator isJson = JSONValidator.from(valueJson);
        if (isJson.validate() && isJsonObject(isJson)) {
            JSONObject parse = JSONObject.parse(valueJson);
            // relative range
            RelativeDateRange range = parse.to(RelativeDateRange.class);
            if (Objects.nonNull(range) && range.validate()) {
                valueJson = new JSONArray(range.getFrom().getDate(), range.getTo().getDate()).toJSONString();
            }
            // relative
            RelativeDate date = parse.to(RelativeDate.class);
            if (Objects.nonNull(date) && date.validate()) {
                valueJson = date.getDate().toString();
            }
        } else {
//            SpecialValue specialValue = EnumUtil.fromStringQuietly(SpecialValue.class, valueJson);
//            if (Objects.nonNull(specialValue)) {
//                valueJson = switch (specialValue) {
//                    case $CurrentUserAccount -> UserContext.getCurrentUserAccount();
//                    case $CurrentUserId -> UserContext.getCurrentUserId().toString();
//                };
//            }
        }
    }

    private static boolean isJsonObject(JSONValidator isJson) {
        return isJson.getType().name().equals(JSON_OBJECT);
    }

    public static String toString(List<Where> list) {
        return list.stream().map(Where::toString)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(AND));
    }

    public enum Op {
        Eq("="),
        Not("<>"),
        Gt(">"),
        Lt("<"),
        Gte(">="),
        Lte("<="),
        Reg("~", true),
        ILike("~~*", true),
        Like("~~", true),
        In("in"),
        Between("between"),

        IsNull("is null", false, true),
        NotNull("is not null", false, true),
        ;
        final String code;
        final Boolean notNumber;
        final Boolean noValue;

        Op(String code) {
            this(code, false);
        }

        Op(String code, Boolean notNumber) {
            this(code, notNumber, false);
        }

        Op(String code, Boolean notNumber, Boolean noValue) {
            this.code = code;
            this.notNumber = notNumber;
            this.noValue = noValue;
        }
    }

    public String setOperatorBy(String name) {
        if (Objects.nonNull(name)) {
            for (Op op : Op.values()) {
                int index = name.lastIndexOf(op.name());
                if (index > 0 && index + op.name().length() == name.length()) {
                    this.operator = op;
                    return name.substring(0, index);
                }
            }
        }
        this.operator = Op.Eq;
        return name;
    }

    public static Op getOpBy(String any) {
        if (Objects.nonNull(any)) {
            for (Op op : Op.values()) {
                if (Objects.equals(op.name(), any) || Objects.equals(op.code, any)) {
                    return op;
                }
            }
        }
        return null;
    }
}
