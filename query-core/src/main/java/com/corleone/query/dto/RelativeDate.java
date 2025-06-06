package com.corleone.query.dto;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class RelativeDate {

    private String relative;
    private Integer quantity;
    private String unit;

    public RelativeDate(String relative, Integer quantity, String unit) {
        this.quantity = quantity;
        this.relative = relative;
        this.unit = unit;
    }

    enum Relative {
        Last, Now, Future
    }

    enum Unit {
        Year, Quarter, Month, Week, Day, WorkDay
    }

    public Date getDate() {
        Relative relativeEnum = Relative.valueOf(relative);
        DateTime dateTime = new DateTime();
        if (relativeEnum != Relative.Now) {
            if (ObjectUtil.hasEmpty(relative, unit)) {
                throw new IllegalArgumentException();
            }
            quantity = Math.abs(NumberUtil.nullToZero(quantity));
            if (relativeEnum == Relative.Last) {
                quantity = -quantity;
            }
            DateField dateField = DateField.DAY_OF_YEAR;
            Unit unitEnum = Unit.valueOf(unit);
            if (unitEnum == Unit.WorkDay) {
                int count = quantity;
                int step = quantity > 0 ? 1 : -1;
                while (count != 0) {
                    dateTime.offset(dateField, step);
                    if (!dateTime.isWeekend()) {
                        count -= step;
                    }
                }
            } else {
                switch (unitEnum) {
                    case Year -> dateField = DateField.YEAR;
                    case Quarter -> {
                        dateField = DateField.MONTH;
                        quantity *= 3;
                    }
                    case Month -> dateField = DateField.MONTH;
                    case Week -> dateField = DateField.WEEK_OF_YEAR;
                }
                dateTime.offset(dateField, quantity);
            }
            dateTime = switch (relativeEnum) {
                case Last -> DateUtil.truncate(dateTime, dateField);
                case Future -> DateUtil.ceiling(dateTime, dateField);
                default -> dateTime;
            };
        }
        return dateTime;
    }

    public boolean validate() {
        return EnumUtil.fromStringQuietly(Relative.class, relative) != null;
    }

    public static void main(String[] args) {
        System.out.println(new RelativeDate("Now", 0, "").getDate());
        System.out.println(new RelativeDate("Last", 1, "Week").getDate());
        System.out.println(new RelativeDate("Last", 0, "Week").getDate());
        System.out.println(new RelativeDate("Future", 1, "Week").getDate());
        System.out.println(new RelativeDate("Future", 0, "Week").getDate());
        System.out.println(new RelativeDate("Last", 5, "WorkDay").getDate());
        System.out.println(new RelativeDate("Future", 5, "WorkDay").getDate());
    }
}
